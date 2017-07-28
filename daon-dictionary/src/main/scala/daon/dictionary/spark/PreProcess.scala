package daon.dictionary.spark

import daon.analysis.ko.util.Utils
import daon.dictionary.spark.SejongToJson.{BaseMorpheme, Morpheme, copy}
import org.apache.commons.lang3.time.StopWatch
import org.apache.spark.sql.{Dataset, _}

import scala.collection.mutable.ArrayBuffer

object PreProcess {

  case class Word(seq: Int, word: String, tag: String, freq: Long)

  case class Sentence(sentence: String, var eojeols: Seq[Eojeol] = ArrayBuffer[Eojeol](), var word_seqs: Seq[Int] = ArrayBuffer[Int]())

  case class Eojeol(seq: Long, var surface: String, offset: Long, var morphemes: Seq[Morpheme] = ArrayBuffer[Morpheme]())

  case class Morpheme(seq: Int, word: String, tag: String,
                      p_outer_seq: Option[Int] = None, p_outer_word: Option[String] = None, p_outer_tag: Option[String] = None,
                      n_outer_seq: Option[Int] = None, n_outer_word: Option[String] = None, n_outer_tag: Option[String] = None,
                      p_inner_seq: Option[Int] = None, p_inner_word: Option[String] = None, p_inner_tag: Option[String] = None,
                      n_inner_seq: Option[Int] = None, n_inner_word: Option[String] = None, n_inner_tag: Option[String] = None
                     )

  case class ProcessedData(rawSentences: Dataset[Sentence], words: Dataset[Word])

  val SENTENCES_INDEX_TYPE = "train_sentences_v2/sentence"

  var wordMap: Map[String, Int] = Map[String, Int]()

  def main(args: Array[String]) {

    val stopWatch = new StopWatch

    stopWatch.start()

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
//      .master("spark://daon.spark:7077")
      .config("es.nodes", "daon.es")
      .config("es.port", "9200")
      .config("es.index.auto.create", "true")
      .getOrCreate()

    process(spark)

    stopWatch.stop()

    println("total elapsed time : " + stopWatch.getTime + " ms")

  }

  def process(spark: SparkSession): ProcessedData = {


    //1. read es_sentences
    //2. make words (seq, word, tag, freq)
    //3. join es_sentences + words => raw_sentences
    //4. make new sentences

    val esSentencesDF = readESSentences(spark)

    val wordsDF = makeWords(spark)

    val rawSentencesDF = createRawSentences(spark, esSentencesDF)

    createSentencesView(spark)

    esSentencesDF.unpersist()

    ProcessedData(rawSentencesDF, wordsDF)
  }

  def readESSentences(spark: SparkSession): Dataset[Row] = {
    // read from es
    val options = Map("es.read.field.exclude" -> "word_seqs")

    val esSentenceDF = spark.read.format("es").options(options).load(SENTENCES_INDEX_TYPE)
//      .limit(1000)

    esSentenceDF.createOrReplaceTempView("es_sentence")
    esSentenceDF.cache()

    esSentenceDF
  }

  def makeWords(spark: SparkSession): Dataset[Word] = {
    import spark.implicits._

    val wordsDF = spark.sql(
      """
         select (row_number() over (order by word asc)) + 10 as seq, word, tag, count(*) as freq
         from
         (
           SELECT
                  morpheme.word as word,
                  morpheme.tag as tag
           FROM (
             SELECT eojeol.surface as surface, eojeol.seq, eojeol.offset, eojeol.morphemes as morphemes
             FROM es_sentence
             LATERAL VIEW explode(eojeols) exploded_eojeols as eojeol
           )
           LATERAL VIEW explode(morphemes) exploded_morphemes as morpheme
         ) as m
         where tag not in ('SL','SH','SN','NA')
         group by word, tag
         order by word asc
      """).as[Word]

    wordsDF.createOrReplaceTempView("words")
    wordsDF.cache()

    makeWordsMap(wordsDF)

    wordsDF
  }

  private def makeWordsMap(wordsDF: Dataset[Word]): Unit = {
    wordMap = wordsDF.collect().map(w => {

      val seq = w.seq
      val word = w.word
      val tag = w.tag

      val key = getKey(word, tag)

//      println(key, seq, freq)
      key -> seq
    }).toMap[String, Int]
  }

  def createRawSentences(spark: SparkSession, esDF: Dataset[Row]): Dataset[Sentence] = {
    import spark.implicits._

    val rawSenetencesDF = esDF.map(row =>{
      val sentence = row.getAs[String]("sentence")
      val eojeols = row.getAs[Seq[Row]]("eojeols")
      val s = Sentence(sentence)


      val eHead = eojeols.head
      val elast = eojeols.last

      eojeols.indices.foreach(e=>{
        val eojeol = eojeols(e)

        var prevOuter = None : Option[BaseMorpheme]
        var nextOuter = None : Option[BaseMorpheme]

//        if(eojeol != eHead){
//          prevOuter = Option(copy(eojeols(e-1).getAs[Seq[Row]]("morphemes").last))
//        }
//
//        if(eojeol != elast){
//          nextOuter = Option(copy(eojeols(e+1).morphemes.head))
//        }


        val eojeolSeq = eojeol.getAs[Long]("seq")
        val surface = eojeol.getAs[String]("surface")
        val offset = eojeol.getAs[Long]("offset")

        val ne = Eojeol(seq = eojeolSeq, surface = surface, offset = offset)

        val morphemes = eojeol.getAs[Seq[Row]]("morphemes")

        morphemes.indices.foreach(m=>{
          val morpheme = morphemes(m)

          val word = morpheme.getAs[String]("word")
          val tag = morpheme.getAs[String]("tag")

          val seq = getSeq(word, tag).get

          s.word_seqs :+= seq

          var p_outer_seq = None : Option[Int]
          var p_outer_word = None : Option[String]
          var p_outer_tag = None : Option[String]
          p_outer_word = Option(morpheme.getAs[String]("p_outer_word"))
          p_outer_tag = Option(morpheme.getAs[String]("p_outer_tag"))

          if(p_outer_word.isDefined) {
            p_outer_seq = getSeq(p_outer_word.get, p_outer_tag.get)
          }

          var n_outer_seq = None : Option[Int]
          var n_outer_word = None : Option[String]
          var n_outer_tag = None : Option[String]
          n_outer_word = Option(morpheme.getAs[String]("n_outer_word"))
          n_outer_tag = Option(morpheme.getAs[String]("n_outer_tag"))

          if(n_outer_word.isDefined) {
            n_outer_seq = getSeq(n_outer_word.get, n_outer_tag.get)
          }

          var p_inner_seq = None : Option[Int]
          var p_inner_word = None : Option[String]
          var p_inner_tag = None : Option[String]
          p_inner_word = Option(morpheme.getAs[String]("p_inner_word"))
          p_inner_tag = Option(morpheme.getAs[String]("p_inner_tag"))

          if(p_inner_seq.isDefined) {
            p_inner_seq = getSeq(p_inner_word.get, p_inner_tag.get)
          }

          var n_inner_seq = None : Option[Int]
          var n_inner_word = None : Option[String]
          var n_inner_tag = None : Option[String]
          n_inner_word = Option(morpheme.getAs[String]("n_inner_word"))
          n_inner_tag = Option(morpheme.getAs[String]("n_inner_tag"))

          if(n_inner_word.isDefined) {
            n_inner_seq = getSeq(n_inner_word.get, n_inner_tag.get)
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
//          //            println(seq)
//            s"(${m.seq}:${m.word}-${m.tag})"
//        }).mkString(", "))

      })

      s
    })

    rawSenetencesDF.createOrReplaceTempView("raw_sentences")
    rawSenetencesDF.cache()

//    rawSenetencesDF.show(10, truncate = false)

    rawSenetencesDF
  }

  def getSeq(word: String, tag: String): Option[Int] = {

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

  def copy(morpheme: Morpheme): BaseMorpheme = {
    val seq = morpheme.seq
    val word = morpheme.word
    val tag = morpheme.tag

    BaseMorpheme(seq, word, tag)
  }


  def createSentencesView(spark: SparkSession): Unit = {

    val sentencesDF = spark.sql(
      """
        | SELECT
        |        seq as eojeol_seq,
        |        offset as eojeol_offset,
        |        surface,
        |        morpheme.seq as word_seq,
        |        morpheme.word,
        |        morpheme.tag,
        |        morpheme.p_outer_seq as p_outer_seq,
        |        morpheme.p_outer_word as p_outer_word,
        |        morpheme.p_outer_tag as p_outer_tag,
        |        morpheme.n_outer_seq as n_outer_seq,
        |        morpheme.n_outer_word as n_outer_word,
        |        morpheme.n_outer_tag as n_outer_tag,
        |        morpheme.p_inner_seq as p_inner_seq,
        |        morpheme.p_inner_word as p_inner_word,
        |        morpheme.p_inner_tag as p_inner_tag,
        |        morpheme.n_inner_seq as n_inner_seq,
        |        morpheme.n_inner_word as n_inner_word,
        |        morpheme.n_inner_tag as n_inner_tag
        | FROM (
        |   SELECT eojeol.surface as surface, eojeol.seq, eojeol.offset, eojeol.morphemes as morphemes
        |   FROM raw_sentences
        |   LATERAL VIEW explode(eojeols) exploded_eojeols as eojeol
        | )
        | LATERAL VIEW explode(morphemes) exploded_morphemes as morpheme
        |
          """.stripMargin)

    sentencesDF.createOrReplaceTempView("sentences")
    sentencesDF.cache()

//    sentencesDF.show(10, truncate = false)
  }

}
