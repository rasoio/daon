package daon.dictionary.spark

import java.io.{ByteArrayOutputStream, FileOutputStream}
import java.time.LocalDateTime
import java.{lang, util}

import daon.analysis.ko.proto.Model
import daon.dictionary.spark.MakeWordsFST.Word
import org.apache.commons.lang.time.StopWatch
import org.apache.spark.sql._
import org.elasticsearch.spark._

import scala.collection.mutable.ArrayBuffer

object MakeModel {

  case class ModelData(seq: Long, create_date: String, data: Array[Byte], size: Long)

  case class InnerWord(surface: String, wordSeqs: Array[Int], freq: Long)
  case class InnerWordTemp(surface: String, wordSeqs: ArrayBuffer[Int])

  val WORDS_INDEX_TYPE = "words_v2/word"
  val SENTENCES_INDEX_TYPE = "train_sentences_v2/sentence"

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

    val rawSentenceDF: DataFrame = readSentences(spark)

    createSentencesView(spark)

    val wordDF: Dataset[Word] = readWords(spark)

    //사전 단어 최대 노출 빈도
    val maxFreq = wordDF.groupBy().max("freq").collect()(0).getLong(0)
    println("maxFreq : " + maxFreq)

    val wordsFstByte = MakeWordsFST.makeFST(spark, rawSentenceDF, wordDF)

    val dictionaryMap = MakeWordsFST.getDictionaryMap

    println("words size : " + wordsFstByte.size())

    val connectionFstByte = MakeConnectionFST.makeFST(spark, rawSentenceDF)

    println("connection size : " + connectionFstByte.size())

    val tagTransMap = MakeTagTrans.makeTagTransMap(spark)

    val builder = Model.newBuilder

    builder.setMaxFreq(maxFreq)

//    builder.setDictionaryFst(dictionaryFstByte)
    builder.setWordsFst(wordsFstByte)
    builder.setConnectionFst(connectionFstByte)

    builder.putAllDictionary(dictionaryMap)
    builder.putAllTagTrans(tagTransMap)

    val model = builder.build

    writeModelToFile(model)
//    writeModel(spark, model)

    stopWatch.stop()

    println("total elapsed time : " + stopWatch.getTime + " ms")

  }

  private def writeModelToFile(model: Model) = {
    val output = new FileOutputStream("/Users/mac/work/corpus/model/model7.dat")

    model.writeTo(output)

    output.close()
  }

  private def writeModelToES(spark: SparkSession, model: Model) = {

    val output = new ByteArrayOutputStream()

    model.writeTo(output)

    output.close()

    val data = output.toByteArray

    val modelData = ModelData(System.currentTimeMillis(), LocalDateTime.now.toString, data, data.size)

    val rdd = spark.sparkContext.makeRDD(Seq(modelData))

    rdd.saveToEs("models/model", Map("es.mapping.id" -> "seq"))

  }

  def readWords(spark: SparkSession): Dataset[Word] = {
    import spark.implicits._

    val wordDF = spark.read.format("es").load(WORDS_INDEX_TYPE).as[Word]

    wordDF.cache()
    wordDF.createOrReplaceTempView("words")
    wordDF
  }

  def readSentences(spark: SparkSession): Dataset[Row] = {
    // read from es
    val options = Map("es.read.field.exclude" -> "word_seqs")

    val rawSentenceDF = spark.read.format("es").options(options).load(SENTENCES_INDEX_TYPE)
      .limit(1000)

    rawSentenceDF.createOrReplaceTempView("raw_sentence")
    rawSentenceDF.cache()

    rawSentenceDF
  }

  def createSentencesView(spark: SparkSession): Unit = {
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

}
