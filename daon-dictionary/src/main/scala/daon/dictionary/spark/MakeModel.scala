package daon.dictionary.spark

import java.io.FileOutputStream
import java.{lang, util}
import java.util.Collections

import daon.analysis.ko.fst.DaonFSTBuilder
import daon.analysis.ko.model._
import daon.analysis.ko.proto.Model
import org.apache.commons.lang.time.StopWatch
import org.apache.spark.sql._

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}

object MakeModel {

  case class Word(seq: Long, word: String, tag: String, freq: Long, desc: String)

  case class InnerWord(surface: String, wordSeqs: Array[Int], freq: Long)
  case class InnerWordTemp(surface: String, wordSeqs: ArrayBuffer[Int])

  //모델 파일 저장 경로
  val filePath = "/Users/mac/work/corpus/model/model2.dat"

  def main(args: Array[String]) {

    val stopWatch = new StopWatch

    stopWatch.start()

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
      .config("es.nodes", "localhost")
      .config("es.port", "9200")
      .config("es.index.auto.create", "true")
      .getOrCreate()

    import spark.implicits._

    val rawSentenceDF: DataFrame = readSentences(spark)

    createSentencesView(spark)

    val wordDF: Dataset[Word] = readWords(spark)

    //사전 단어 최대 노출 빈도
    val maxFreq = wordDF.groupBy().max("freq").collect()(0).getLong(0)
    println("maxFreq : " + maxFreq)

    //사전 단어
    val words = wordDF.collect()

    val dictionaryMap = makeDictionaryMap(words)

    val dictionaryKeywordSeqs = makeDicKeywordSeqs(words)

    val innerWordKeywordSeqs = makeInnerKeywordSeqs(spark, rawSentenceDF)

    //빌드 fst
    val dictionaryFst = DaonFSTBuilder.create.buildIntsFst(dictionaryKeywordSeqs)
    val dictionaryFstByte = DaonFSTBuilder.toByteString(dictionaryFst)

    val innerWordFst = DaonFSTBuilder.create.buildPairFst(innerWordKeywordSeqs)
    val innerWordFstByte = DaonFSTBuilder.toByteString(innerWordFst)

    println("dictionary size : " + dictionaryKeywordSeqs.size() + ", innerWords size : " + innerWordKeywordSeqs.size())

    val tagsMap = makeTagsMap(spark)

    val tagTransMap = makeTagTransMap(spark)

    val innerMap = makeInnerMap(spark)

    val outerMap = makeOuterMap(spark)

    val builder = Model.newBuilder

    builder.setMaxFreq(maxFreq)

    builder.setDictionaryFst(dictionaryFstByte)
    builder.setInnerWordFst(innerWordFstByte)

    builder.putAllDictionary(dictionaryMap)
    builder.putAllTags(tagsMap)
    builder.putAllTagTrans(tagTransMap)
    builder.putAllInner(innerMap)
    builder.putAllOuter(outerMap)

    val model = builder.build

    writeModel(model)

    stopWatch.stop()

    println("total elapsed time : " + stopWatch.getTime + " ms")

  }

  private def writeModel(model: Model) = {

    val output = new FileOutputStream(filePath)

    model.writeTo(output)

    output.close()
  }

  private def makeOuterMap(spark: SparkSession) = {
    val outerMap = new util.HashMap[Integer, lang.Float]()

    //outer 연결 정보 모델
    val outerDF = spark.sql(
      """
        select
            p_outer_seq as pOuterSeq,
            word_seq as wordSeq,
            count(*) as freq
        from sentence
        where p_outer_seq > 0
        and  word_seq > 0
        and tag not in ('SS','SP','SN','SH','SL','SW','SE','SO')
        and p_outer_tag not in ('SS','SP','SN','SH','SL','SW','SE','SO')
        group by p_outer_seq, word_seq
      """)

    val outerMaxFreq = outerDF.groupBy().max("freq").collect()(0).getLong(0)

    outerDF.collect().foreach(row => {

      val key = (row.getAs[Long]("pOuterSeq") + "|" + row.getAs[Long]("wordSeq")).hashCode
      val freq = row.getAs[Long]("freq").toFloat / outerMaxFreq

      outerMap.put(key, freq)
    })

    outerMap
  }

  private def makeInnerMap(spark: SparkSession) = {

    val innerMap = new util.HashMap[Integer, lang.Float]()

    //inner 연결 정보 모델
    val innerDF = spark.sql(
      """
        select
            word_seq as wordSeq,
            n_inner_seq as nInnerSeq,
            count(*) as freq
        from sentence
        where word_seq > 0
        and n_inner_seq > 0
        and tag not in ('SS','SP','SN','SH','SL','SW','SE','SO')
        and n_inner_tag not in ('SS','SP','SN','SH','SL','SW','SE','SO')
        group by  word_seq, n_inner_seq
      """)

    val innerMaxFreq = innerDF.groupBy().max("freq").collect()(0).getLong(0)

    innerDF.collect().foreach(row => {
      val key = (row.getAs[Long]("wordSeq") + "|" + row.getAs[Long]("nInnerSeq")).hashCode
      val freq = row.getAs[Long]("freq").toFloat / innerMaxFreq

      innerMap.put(key, freq)
    })

    innerMap
  }

  private def makeTagTransMap(spark: SparkSession) = {

    val tagTransMap = new util.HashMap[Integer, lang.Float]()

    //tag 전이 확률
    val tagTransFreqDF = spark.sql(
      """
        select tag, n_inner_tag as nInnerTag, count(*) as freq
        from sentence
        where n_inner_tag is not null
        group by tag, n_inner_tag
        order by count(*) desc
      """)

    val tagTransMaxFreq = tagTransFreqDF.groupBy().max("freq").collect()(0).getLong(0)

    tagTransFreqDF.collect().foreach(row => {

      val key = (row.getAs[String]("tag") + "|" + row.getAs[String]("nInnerTag")).hashCode
      val freq = row.getAs[Long]("freq").toFloat / tagTransMaxFreq

      tagTransMap.put(key, freq)
    })

    tagTransMap
  }

  private def makeTagsMap(spark: SparkSession) = {

    val tagsMap = new util.HashMap[Integer, lang.Float]()

    //tag 노출 확률
    val tagFreqDF = spark.sql(
      """
        select tag, count(*) as freq
        from sentence
        where p_inner_tag is null
        group by tag
        order by count(*) desc
      """)

    val tagMaxFreq = tagFreqDF.groupBy().max("freq").collect()(0).getLong(0)

    tagFreqDF.collect().foreach(row => {

      val key = row.getAs[String]("tag").hashCode
      val freq = row.getAs[Long]("freq").toFloat / tagMaxFreq

      tagsMap.put(key, freq)
    })

    tagsMap
  }

  private def makeInnerKeywordSeqs(spark: SparkSession, rawSentenceDF: DataFrame) = {

    val innerWordKeywordSeqs = new util.ArrayList[KeywordSeq]

    val innerWords = makeInnerWords(spark, rawSentenceDF)
    innerWords.show()

    //어절 부분 사전
    innerWords.collect().foreach(innerWord => {
      val word = innerWord.surface
      val seqs = innerWord.wordSeqs
      val freq = innerWord.freq

      val keywordSeq = new KeywordSeq(word, seqs)
      keywordSeq.setFreq(freq)
      innerWordKeywordSeqs.add(keywordSeq)
    })

    Collections.sort(innerWordKeywordSeqs)

    innerWordKeywordSeqs
  }

  private def makeDicKeywordSeqs(words: Array[Word]) = {

    val dictionaryKeywordSeqs = new util.ArrayList[KeywordSeq]

    //fst 용
    val groupWords = words.groupBy(w => w.word).mapValues(word => word.map(w => w.seq.toInt))
    groupWords.foreach(keyword => {
      val word = keyword._1
      val seq = keyword._2

      val keywordSeq = new KeywordSeq(word, seq)
      dictionaryKeywordSeqs.add(keywordSeq)
    })

    Collections.sort(dictionaryKeywordSeqs)

    dictionaryKeywordSeqs
  }

  private def makeDictionaryMap(words: Array[Word]) = {

    val dictionaryMap = new util.HashMap[Integer, Model.Keyword]()

    words.foreach(keyword => {

      val seq = keyword.seq.toInt
      //model dictionary 용
      val newKeyword = daon.analysis.ko.proto.Model.Keyword.newBuilder.setSeq(seq).setWord(keyword.word).setTag(keyword.tag).setFreq(keyword.freq).build
      dictionaryMap.put(seq, newKeyword)

    })

    dictionaryMap
  }

  private def readWords(spark: SparkSession) = {
    import spark.implicits._

    val wordDF = spark.read.format("es").load("dictionary/words").as[Word]

    wordDF.cache()
    wordDF.createOrReplaceTempView("words")
    wordDF
  }

  private def readSentences(spark: SparkSession) = {
    // read from es
    val options = Map("es.read.field.exclude" -> "word_seqs")

    val rawSentenceDF = spark.read.format("es").options(options).load("corpus/sentences")

    rawSentenceDF.createOrReplaceTempView("raw_sentence")
    rawSentenceDF.cache()

    rawSentenceDF
  }

  private def createSentencesView(spark: SparkSession) = {
    val sentenceDF = spark.sql(
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
        |   FROM raw_sentence
        |   LATERAL VIEW explode(eojeols) exploded_eojeols as eojeol
        | )
        | LATERAL VIEW explode(morphemes) exploded_morphemes as morpheme
        |
      """.stripMargin)

    sentenceDF.createOrReplaceTempView("sentence")
    sentenceDF.cache()
  }

  def makeInnerWords(spark: SparkSession, rawSentenceDF: DataFrame) = {
    import spark.implicits._

    val innerWordsDf = rawSentenceDF.flatMap(row => {
      val sentence = row.getAs[String]("sentence")
      val eojeols = row.getAs[Seq[Row]]("eojeols")


      val words = ArrayBuffer[InnerWordTemp]()

      eojeols.indices.foreach(e=> {
        val eojeol = eojeols(e)
        val surface_org = eojeol.getAs[String]("surface")
        val morphemes = eojeol.getAs[Seq[Row]]("morphemes")

        var surface = surface_org

        val results = surface.split("[^가-힣ㄱ-ㅎㅏ-ㅣ]")

        val flag = addWords(results, morphemes, words)

        if(flag){
          println("erro find ====>", sentence, surface_org)
        }
      })

      words
    }).as[InnerWordTemp]

    innerWordsDf.createOrReplaceTempView("inner_words")

    val innerWords = spark.sql(
      """
      select surface, wordSeqs, count(*) freq
        from inner_words
        group by surface, wordSeqs
        order by surface asc
      """)

    innerWords.cache()
    innerWords.as[InnerWord]
  }

  def addWords(results: Array[String], morphemes: Seq[Row], words: ArrayBuffer[InnerWordTemp]): Boolean ={

    var lastIdx = 0
    for (surface <- results) {

      val wordSeqs = ArrayBuffer[Int]()

      breakable {
        for (i <- lastIdx until morphemes.size) {

          val morpheme = morphemes(i)
          val seq = morpheme.getAs[Long]("seq").toInt
          val w = morpheme.getAs[String]("word")
          val tag = morpheme.getAs[String]("tag")

          //사전 단어가 아니거나, 특수문자인 경우 break
          if (seq == 0 || tag.startsWith("S")) {
            lastIdx = i
            break
          }

          wordSeqs += seq
        }
      }

      //어절 결과가 있고, 사전 단어의 수가 2개 이상인 경우만 적용
      if(surface.length > 0 && wordSeqs.size > 1) {

        //말뭉치 오류 검증용 조건 정의
        if(surface == "네놈들한테" && wordSeqs(0) == 31708){
          return true
        }

        val word = InnerWordTemp(surface, wordSeqs)
        words += word
      }

    }

    return false
  }

}
