package daon.spark

import java.io.{File, FileOutputStream}

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.sql._
import org.elasticsearch.spark.sql._
//import org.apache.commons.io.{FileUtils, IOUtils}

import scala.collection.mutable.ArrayBuffer

object SentenceDataMig {

  val CONFIG : Config = {
    ConfigFactory.load("application.conf")
  }

  val SEJONG_SENTENCES_INDEX_TYPE = "sejong_sentences/sentence"
  val TRAIN_SENTENCES_INDEX_TYPE = "sejong_train_sentences_v3/sentence"
  val TEST_SENTENCES_INDEX_TYPE = "sejong_test_sentences_v3/sentence"
  val NIADIC_SENTENCES_INDEX_TYPE = "niadic_sentences_v3/sentence"

  case class Sentence(sentence: String, var eojeols: Seq[Eojeol] = ArrayBuffer[Eojeol]())

  case class Eojeol(seq: Long, var surface: String, var morphemes: Seq[Morpheme] = ArrayBuffer[Morpheme]())

  case class Morpheme(seq: Long, word: String, tag: String)

  val index_type: String = CONFIG.getString("index_type")
  val path: String = CONFIG.getString("index.sentences.jsonPath")

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

//    val read = FileUtils.readFileToString(new File("/Users/mac/work/corpus/word.log"), "UTF-8")


  }


  private def readEsWriteJson(spark: SparkSession) = {

    val sejongSentenceDF = spark.read.format("es").load(index_type)

    sejongSentenceDF.coalesce(1).write.format("org.apache.spark.sql.json").mode("overwrite").save(path)
  }


  private def readJsonWriteEs(spark: SparkSession) = {

    val sentenceDF = spark.read.json("/Users/mac/work/corpus/updated_sentences_v3")

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
      var sentence = row.getAs[String]("sentence")
      val eojeols = row.getAs[Seq[Row]]("eojeols")

      sentence = replace(sentence)
      val s = Sentence(sentence)

      eojeols.indices.foreach(e=>{
        val eojeol = eojeols(e)

        var surface = eojeol.getAs[String]("surface")

        surface = replace(surface)

        val ne = Eojeol(seq = e, surface = surface)

        val morphemes = eojeol.getAs[Seq[Row]]("morphemes")

        morphemes.indices.foreach(m=>{
          val morpheme = morphemes(m)

          var word = morpheme.getAs[String]("word")

          word = replace(word)

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

  private def replace(str: String): String ={

    str.replaceAll("‘", "'").replaceAll("’", "'")
      .replaceAll("“", "\"").replaceAll("”", "\"")
      .replaceAll("∼", "~")
      .replaceAll("～", "~")
//      .replaceAll("·", "·")
//      .toLowerCase
  }

  private def write(txt: String): Unit = {
//    IOUtils.write(txt + System.lineSeparator, out, "UTF-8")
  }

  private def isSplitTag(tag: String): Boolean = {
    tag.startsWith("S") || tag == "NA"
  }


}
