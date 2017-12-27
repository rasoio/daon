package daon.spark

import daon.core.data
import daon.core.util.Utils
import org.apache.commons.lang3.time.StopWatch
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.{Dataset, _}
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable.ArrayBuffer

object PreProcess {

  case class Word(var seq: Int, word: String, tag: String, freq: Long)

  case class Sentence(sentence: String, var eojeols: Seq[Eojeol] = ArrayBuffer[Eojeol]())

  case class Eojeol(seq: Long, var surface: String, var morphemes: Seq[Morpheme] = ArrayBuffer[Morpheme]())

  case class Morpheme(seq: Int, word: String, tag: String,
                      p_outer_seq: Option[Int] = None, p_outer_word: Option[String] = None, p_outer_tag: Option[String] = None,
                      n_outer_seq: Option[Int] = None, n_outer_word: Option[String] = None, n_outer_tag: Option[String] = None,
                      p_inner_seq: Option[Int] = None, p_inner_word: Option[String] = None, p_inner_tag: Option[String] = None,
                      n_inner_seq: Option[Int] = None, n_inner_word: Option[String] = None, n_inner_tag: Option[String] = None
                     )

  case class MorphemeTemp(seq: Int, word: String, tag: String)

  case class ProcessedData(rawSentences: Dataset[Sentence], sentences: Dataset[Row], words: Array[Word], maxFreq: Long)

  val SENTENCES_INDEX_TYPE = "train_sentences/sentence"

  def main(args: Array[String]) {

    val stopWatch = new StopWatch

    stopWatch.start()

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
//      .master("spark://daon.spark:7077")
      .config("es.nodes", "localhost")
      .config("es.port", "9200")
      .getOrCreate()

    process2(spark)

    stopWatch.stop()

    println("total elapsed time : " + stopWatch.getTime + " ms")

  }

  def process(spark: SparkSession): ProcessedData = {


    //1. read es_sentences
    //2. make words (seq, word, tag, freq)
    //3. join es_sentences + words => raw_sentences
    //4. make new sentences

    val esSentences = readESSentences(spark)

    val (words, maxFreq) = makeWords(spark)
    val broadcastWordMap = makeWordsMap(spark, words)

//    val broadcastVar = spark.sparkContext.broadcast(wordMap)
//    val broadcastWordMap = broadcastVar.value
    val wordMap = broadcastWordMap.value
    val rawSentences = createRawSentences(spark, esSentences, wordMap)

    val sentences = createSentencesView(spark)

    broadcastWordMap.destroy()
    esSentences.unpersist()

    ProcessedData(rawSentences, sentences, words, maxFreq)
  }


  def process2(spark: SparkSession): Unit = {

    import spark.implicits._

    //1. read es_sentences
    //2. make words (seq, word, tag, freq)
    //3. join es_sentences + words => raw_sentences
    //4. make new sentences

    val esSentences = readESSentences(spark)

    implicit val SensorDataEncoder = Encoders.bean(classOf[data.Sentence])

    val rawSenetencesDF = esSentences.map(row =>{
      val eojeols = row.getAs[Seq[Row]]("eojeols")
      val s = new data.Sentence()

      eojeols.indices.foreach(e=>{
        val eojeol = eojeols(e)

        val eojeolSeq = eojeol.getAs[Long]("seq")
        val surface = eojeol.getAs[String]("surface")

        val ne = new data.Eojeol(eojeolSeq.toInt, surface)

        val morphemes = eojeol.getAs[Seq[Row]]("morphemes")

        morphemes.indices.foreach(m=>{
          val morpheme = morphemes(m)

          val word = morpheme.getAs[String]("word")
          val tag = morpheme.getAs[String]("tag")

          val nm = new data.Morpheme(word, tag)
          ne.getMorphemes.add(nm)
        })

        s.getEojeols.add(ne)
      })

      s
    }).as(SensorDataEncoder)

    rawSenetencesDF.show(false)

    val (words, maxFreq) = makeWords2(spark)

    words.foreach(println)
  }

  def readESSentences(spark: SparkSession): Dataset[Row] = {
    // read from es
//    val options = Map("es.read.field.exclude" -> "word_seqs")

    val esSentenceDF = spark.read.format("es").load(SENTENCES_INDEX_TYPE).limit(100)
//      .limit(1000)

    esSentenceDF.createOrReplaceTempView("es_sentence")
    esSentenceDF.cache()

    esSentenceDF
  }


  def makeWords2(spark: SparkSession): (Array[data.Morpheme], Long)  = {

    import spark.implicits._

    //0~10 은 예약 seq (1 : 숫자, 2: 영문/한자)

    import org.apache.spark.sql.Encoder
    import org.apache.spark.sql.Encoders
//    val personEncoder = Encoders.product[Morpheme]

    import org.apache.spark.sql.Encoders
    implicit val SensorDataEncoder = Encoders.bean(classOf[data.Morpheme])

    val df = spark.sql(
      """
         select 0 as seq, word, tag, count(*) as freq
         from
         (
           SELECT
                  morpheme.word as word,
                  morpheme.tag as tag
           FROM (
             SELECT eojeol.morphemes as morphemes
             FROM es_sentence
             LATERAL VIEW explode(eojeols) exploded_eojeols as eojeol
           )
           LATERAL VIEW explode(morphemes) exploded_morphemes as morpheme
         ) as m
         where tag not in ('SL','SH','SN','NA')
         and word is not null
         group by word, tag
         order by word asc
      """).as(SensorDataEncoder)

    df.cache()
    //    wordsDF.coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/words")

    val maxFreq = df.groupBy().max("freq").collect()(0).getLong(0)

    //seq 채번
    var seq = 10
    val words = df.collect().map(w => {
      seq += 1
      w.setSeq(seq)
      w
    })

    df.unpersist()

    (words, maxFreq)
  }



  def makeWords(spark: SparkSession): (Array[Word], Long)  = {
    import spark.implicits._

    //0~10 은 예약 seq (1 : 숫자, 2: 영문/한자)

    val df = spark.sql(
      """
         select 0 as seq, word, tag, count(*) as freq
         from
         (
           SELECT
                  morpheme.word as word,
                  morpheme.tag as tag
           FROM (
             SELECT eojeol.morphemes as morphemes
             FROM es_sentence
             LATERAL VIEW explode(eojeols) exploded_eojeols as eojeol
           )
           LATERAL VIEW explode(morphemes) exploded_morphemes as morpheme
         ) as m
         where tag not in ('SL','SH','SN','NA')
         and word is not null
         group by word, tag
         order by word asc
      """).as[Word]

    df.cache()
//    wordsDF.coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/words")

    val maxFreq = df.groupBy().max("freq").collect()(0).getLong(0)

    //seq 채번
    var seq = 10
    val words = df.collect().map(w => {
      seq += 1
      w.seq = seq
      w
    })

    df.unpersist()

    (words, maxFreq)
  }

  private def makeWordsMap(spark: SparkSession, words: Array[Word]): Broadcast[Map[String, Int]] = {
    val wordMap = words.map(w => {

      val seq = w.seq
      val word = w.word
      val tag = w.tag

      val key = getKey(word, tag)

      key -> seq
    }).toMap[String, Int]


    val broadcastVar = spark.sparkContext.broadcast(wordMap)

    broadcastVar
  }

  def createRawSentences(spark: SparkSession, esDF: Dataset[Row], wordMap: Map[String, Int]): Dataset[Sentence] = {
    import spark.implicits._

    val rawSenetencesDF = esDF.map(row =>{
      val sentence = row.getAs[String]("sentence")
      val eojeols = row.getAs[Seq[Row]]("eojeols")
      val s = Sentence(sentence)

      val eHead = eojeols.head
      val elast = eojeols.last

      eojeols.indices.foreach(e=>{
        val eojeol = eojeols(e)

        var prevOuter = None : Option[MorphemeTemp]
        var nextOuter = None : Option[MorphemeTemp]

        if(eojeol != eHead){
          prevOuter = Option(copy(eojeols(e-1).getAs[Seq[Row]]("morphemes").last, wordMap))
        }

        if(eojeol != elast){
          nextOuter = Option(copy(eojeols(e+1).getAs[Seq[Row]]("morphemes").head, wordMap))
        }

        val eojeolSeq = eojeol.getAs[Long]("seq")
        val surface = eojeol.getAs[String]("surface")

        val ne = Eojeol(seq = eojeolSeq, surface = surface)

        val morphemes = eojeol.getAs[Seq[Row]]("morphemes")

        val head = morphemes.head
        val last = morphemes.last

        morphemes.indices.foreach(m=>{
          val morpheme = morphemes(m)

          val word = morpheme.getAs[String]("word")
          val tag = morpheme.getAs[String]("tag")
          val seq = getSeq(word, tag, wordMap).get

          var p_outer_seq = None : Option[Int]
          var p_outer_word = None : Option[String]
          var p_outer_tag = None : Option[String]

          if(morpheme == head && prevOuter.isDefined) {
            val p_outer = prevOuter.get
            p_outer_word = Option(p_outer.word)
            p_outer_tag = Option(p_outer.tag)
            p_outer_seq = Option(p_outer.seq)
          }

          var n_outer_seq = None : Option[Int]
          var n_outer_word = None : Option[String]
          var n_outer_tag = None : Option[String]

          if(morpheme == last && nextOuter.isDefined) {
            val n_outer = nextOuter.get
            n_outer_word = Option(n_outer.word)
            n_outer_tag = Option(n_outer.tag)
            n_outer_seq = Option(n_outer.seq)
          }

          var p_inner_seq = None : Option[Int]
          var p_inner_word = None : Option[String]
          var p_inner_tag = None : Option[String]

          if(morpheme != head){
            val prevInner = copy(morphemes(m-1), wordMap)
            p_inner_word = Option(prevInner.word)
            p_inner_tag = Option(prevInner.tag)
            p_inner_seq = Option(prevInner.seq)
          }

          var n_inner_seq = None : Option[Int]
          var n_inner_word = None : Option[String]
          var n_inner_tag = None : Option[String]

          if(morpheme != last) {
            val nextInner = copy(morphemes(m+1), wordMap)
            n_inner_word = Option(nextInner.word)
            n_inner_tag = Option(nextInner.tag)
            n_inner_seq = Option(nextInner.seq)
          }

          val nm = Morpheme(seq, word, tag,
            p_outer_seq, p_outer_word, p_outer_tag,
            n_outer_seq, n_outer_word, n_outer_tag,
            p_inner_seq, p_inner_word, p_inner_tag,
            n_inner_seq, n_inner_word, n_inner_tag
          )

//          println(nm)

          ne.morphemes :+= nm
        })

        s.eojeols :+= ne

//        println(ne.surface, ne.morphemes.map(m => {
//            s"(${m.seq}:${m.word}-${m.tag})"
//        }).mkString(", "))

      })

      s
    })

    rawSenetencesDF.createOrReplaceTempView("raw_sentences")
    rawSenetencesDF.cache()

    rawSenetencesDF
  }

  def getSeq(word: String, tag: String, wordMap: Map[String, Int]): Option[Int] = {

    val key = getKey(word, tag)
    var seq = wordMap.get(key)

    if(seq.isEmpty) {
      seq = Option(Utils.getSeq(tag))

      if(seq.isEmpty){
        seq = Option(0)
      }
    }

    seq
  }

  def getKey(word: String, tag: String): String = {
    word + "||" + tag
  }

  def copy(morpheme: Row, wordMap: Map[String, Int]): MorphemeTemp = {
    val word = morpheme.getAs[String]("word")
    val tag = morpheme.getAs[String]("tag")

    val seq = getSeq(word, tag, wordMap).get

    MorphemeTemp(seq, word, tag)
  }


  def createSentencesView(spark: SparkSession): Dataset[Row] = {

    val sentencesDF = spark.sql(
//      """
//        | SELECT
//        |        eojeol_seq,
//        |        surface,
//        |        morpheme.seq as word_seq,
//        |        morpheme.word,
//        |        morpheme.tag,
//        |        morpheme.p_outer_seq as p_outer_seq,
//        |        morpheme.p_outer_word as p_outer_word,
//        |        morpheme.p_outer_tag as p_outer_tag,
//        |        morpheme.n_outer_seq as n_outer_seq,
//        |        morpheme.n_outer_word as n_outer_word,
//        |        morpheme.n_outer_tag as n_outer_tag,
//        |        morpheme.p_inner_seq as p_inner_seq,
//        |        morpheme.p_inner_word as p_inner_word,
//        |        morpheme.p_inner_tag as p_inner_tag,
//        |        morpheme.n_inner_seq as n_inner_seq,
//        |        morpheme.n_inner_word as n_inner_word,
//        |        morpheme.n_inner_tag as n_inner_tag
//        | FROM (
//        |   SELECT eojeol.surface as surface, eojeol.seq as eojeol_seq, eojeol.morphemes as morphemes
//        |   FROM raw_sentences
//        |   LATERAL VIEW explode(eojeols) exploded_eojeols as eojeol
//        | )
//        | LATERAL VIEW explode(morphemes) exploded_morphemes as morpheme
//        |
//          """.stripMargin)

      """
        | SELECT
        |        morpheme.tag,
        |        morpheme.p_outer_tag as p_outer_tag,
        |        morpheme.n_outer_tag as n_outer_tag,
        |        morpheme.p_inner_tag as p_inner_tag,
        |        morpheme.n_inner_tag as n_inner_tag
        | FROM (
        |   SELECT eojeol.morphemes as morphemes
        |   FROM raw_sentences
        |   LATERAL VIEW explode(eojeols) exploded_eojeols as eojeol
        | )
        | LATERAL VIEW explode(morphemes) exploded_morphemes as morpheme
        |
          """.stripMargin)

    sentencesDF.createOrReplaceTempView("sentences")
    sentencesDF.cache()

    //collect
    sentencesDF.count()

    sentencesDF
  }

}
