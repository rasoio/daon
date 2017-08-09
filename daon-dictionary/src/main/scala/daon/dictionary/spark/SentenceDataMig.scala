package daon.dictionary.spark

import org.apache.spark.sql._
import org.elasticsearch.spark.sql._
import daon.analysis.ko.util.Utils

import scala.collection.mutable.ArrayBuffer

object SentenceDataMig {


  val TRAIN_SENTENCES_INDEX_TYPE = "train_sentences_v2/sentence"
  val TEST_SENTENCES_INDEX_TYPE = "test_sentences_v2/sentence"

  case class Sentence(sentence: String, var eojeols: Seq[Eojeol] = ArrayBuffer[Eojeol]())

  case class Eojeol(seq: Long, var surface: String, var morphemes: Seq[Morpheme] = ArrayBuffer[Morpheme]())

  case class Morpheme(seq: Long, word: String, tag: String)


  def main(args: Array[String]) {


    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
      .config("es.nodes", "localhost")
      .config("es.port", "9200")
      .config("es.index.auto.create", "true")
      .getOrCreate()

    readEsWriteJson(spark)

//    readJsonWriteEs(spark)

  }


  private def readEsWriteJson(spark: SparkSession) = {

    // read from es
    val options = Map(
      "es.read.field.as.array.include" -> "word_seqs"
    )

    val trainSentenceDF = spark.read.format("es").options(options).load(TRAIN_SENTENCES_INDEX_TYPE)

    val trainJsonDF = toDF(spark, trainSentenceDF)

    trainJsonDF.coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/train_sentences_v2")

    val testSentenceDF = spark.read.format("es").options(options).load(TEST_SENTENCES_INDEX_TYPE)

    val testJsonDF = toDF(spark, testSentenceDF)

    testJsonDF.coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/test_sentences_v2")

  }


  private def readJsonWriteEs(spark: SparkSession) = {

    val sentenceDF = spark.read.json("/Users/mac/work/corpus/sentence_v3")

//    sentenceDF.show(10, false)

    val cnt = sentenceDF.count()

    // 9:1
    val splitDF = sentenceDF.randomSplit(Array(0.9, 0.1))
    val trainDF = splitDF(0)
    val testDF = splitDF(1)

    println("sentence_v3 total cnt : " + cnt)
    println("train total cnt : " + trainDF.count())
    println("test total cnt : " + testDF.count())

    trainDF.saveToEs("train_sentences_v3/sentence")
    testDF.saveToEs("test_sentences_v3/sentence")

  }

  private def toDF(spark: SparkSession, sentenceDF: Dataset[Row]): Dataset[Sentence] ={
    import spark.implicits._

    val new_df = sentenceDF.map(row =>{
      val sentence = row.getAs[String]("sentence")
      val eojeols = row.getAs[Seq[Row]]("eojeols")
      val s = Sentence(sentence)

      eojeols.indices.foreach(e=>{
        val eojeol = eojeols(e)

        val surface = eojeol.getAs[String]("surface")

        val ne = Eojeol(seq = e, surface = surface)

        val morphemes = eojeol.getAs[Seq[Row]]("morphemes")

        morphemes.indices.foreach(m=>{
          val morpheme = morphemes(m)

          val word = morpheme.getAs[String]("word")
          val tag = morpheme.getAs[String]("tag")

          val nm = Morpheme(m, word, tag)

          ne.morphemes :+= nm
        })

        s.eojeols :+= ne

      })

      s
    })

    new_df
  }


}
