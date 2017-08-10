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
//      .config("spark.executor.memory", "4g")
      .getOrCreate()

    val processedData = PreProcess.process(spark)

    val rawSentenceDF: Dataset[Sentence] = processedData.rawSentences

    val wordDF: Dataset[Word] = processedData.words

    val fstBytes = MakeWordsFST.makeFST(spark, rawSentenceDF, wordDF)

    val dictionaryMap = MakeWordsFST.getDictionaryMap

    val tagTrans = MakeTagTrans.makeTagTransMap(spark)

    val builder = Model.newBuilder

    builder.setWordFst(fstBytes)

    builder.addAllFirstTags(tagTrans.firstTags)
    builder.addAllMiddleTags(tagTrans.middleTags)
    builder.addAllLastTags(tagTrans.lastTags)
    builder.addAllConnectTags(tagTrans.connectTags)

    builder.putAllDictionary(dictionaryMap)

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
