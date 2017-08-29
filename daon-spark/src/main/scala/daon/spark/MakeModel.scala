package daon.spark

import java.io.{ByteArrayOutputStream, FileOutputStream}
import java.time.LocalDateTime
import java.{lang, util}

import daon.analysis.ko.proto.Model
import PreProcess.{Sentence, Word}
import org.apache.commons.lang3.time.StopWatch
import org.apache.spark.sql._
import org.elasticsearch.spark._

import scala.collection.mutable.ArrayBuffer

object MakeModel {

  case class ModelData(seq: Long, create_date: String, data: Array[Byte], size: Long, dictionary_count: Long, elapsed_time: Long)

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
//      .master("spark://daon.spark:7077")
//      .config("es.nodes", "localhost")
//      .config("es.port", "9200")
      .config("es.index.auto.create", "false")
      .getOrCreate()

    makeModel(spark)

  }

  def makeModel(spark: SparkSession) = {

    val stopWatch = new StopWatch
    stopWatch.start()

    val processedData = PreProcess.process(spark)

    val rawSentenceDF: Dataset[Sentence] = processedData.rawSentences

    val words: Array[Word] = processedData.words

    val dictionaryMap = MakeWordsFST.makeDictionaryMap(words)

    val fstBytes = MakeWordsFST.makeFST(spark, rawSentenceDF, words)

    val tagTrans = MakeTagTrans.makeTagTransMap(spark)

    val builder = Model.newBuilder

    builder.putAllDictionary(dictionaryMap)
    builder.setWordFst(fstBytes)
    builder.addAllFirstTags(tagTrans.firstTags)
    builder.addAllMiddleTags(tagTrans.middleTags)
    builder.addAllLastTags(tagTrans.lastTags)
    builder.addAllConnectTags(tagTrans.connectTags)

    val model = builder.build

    stopWatch.stop()

    val elapsedTime = stopWatch.getTime

    println("total elapsed time : " + stopWatch.getTime + " ms")

    //    writeModelToFile(model)
    writeModelToES(spark, model, elapsedTime)

  }

  private def writeModelToFile(model: Model) = {
//    val output = new FileOutputStream("/Users/mac/work/corpus/model/model8.dat")
    val output = new FileOutputStream("/Users/mac/IdeaProjects/daon/daon-core/src/main/resources/daon/analysis/ko/reader/model.dat")

    model.writeTo(output)

    output.close()
  }

  private def writeModelToES(spark: SparkSession, model: Model, elapsedTime: Long) = {

    val output = new ByteArrayOutputStream()

    model.writeTo(output)

    output.close()

    val data = output.toByteArray

    val dictionaryCount = model.getDictionaryCount

    val modelData = ModelData(System.currentTimeMillis(), LocalDateTime.now.toString, data, data.size, dictionaryCount, elapsedTime)

    val rdd = spark.sparkContext.makeRDD(Seq(modelData))

    rdd.saveToEs("models/model", Map("es.mapping.id" -> "seq"))

  }

}
