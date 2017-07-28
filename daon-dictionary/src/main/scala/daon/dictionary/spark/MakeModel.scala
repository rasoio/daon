package daon.dictionary.spark

import java.io.{ByteArrayOutputStream, FileOutputStream}
import java.time.LocalDateTime
import java.{lang, util}

import daon.analysis.ko.proto.Model
import daon.dictionary.spark.PreProcess.{Sentence, Word}
import org.apache.commons.lang3.time.StopWatch
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

      //set new runtime options
//      .config("spark.sql.shuffle.partitions", 4)
      .config("spark.executor.memory", "4g")
      .config("spark.driver.memory", "4g")
      .getOrCreate()

    val processedData = PreProcess.process(spark)

    val rawSentenceDF: Dataset[Sentence] = processedData.rawSentences

    val wordDF: Dataset[Word] = processedData.words

    val fstBytes = MakeWordsFST.makeFST(spark, rawSentenceDF, wordDF)
    val forwardFstByte = fstBytes._1
    val backwardFstByte = fstBytes._2

    val dictionaryMap = MakeWordsFST.getDictionaryMap

    val connFSTs = MakeConnectionFST.makeFST(spark, rawSentenceDF)

    //사전 단어 최대 노출 빈도
//    val maxFreq = wordDF.groupBy().max("freq").collect()(0).getLong(0)
//    println("maxFreq : " + maxFreq)
    val tagTransMap = MakeTagTrans.makeTagTransMap(spark)


    val builder = Model.newBuilder

//    builder.setMaxFreq(maxFreq)

//    builder.setDictionaryFst(dictionaryFstByte)
    builder.setForwardFst(forwardFstByte)
    builder.setBackwardFst(backwardFstByte)
    builder.setInnerFst(connFSTs.inner)
//    builder.setOuterFst(connFSTs.outer)
    builder.setConnectionFst(connFSTs.conn)

    builder.putAllDictionary(dictionaryMap)
    builder.putAllTagTrans(tagTransMap)

    val model = builder.build

    writeModelToFile(model)
//    writeModel(spark, model)

    stopWatch.stop()

    println("total elapsed time : " + stopWatch.getTime + " ms")

  }

  private def writeModelToFile(model: Model) = {
//    val output = new FileOutputStream("/Users/mac/work/corpus/model/model8.dat")
    val output = new FileOutputStream("/Users/mac/IdeaProjects/daon/daon-core/src/main/resources/daon/analysis/ko/reader/model.dat")

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

}
