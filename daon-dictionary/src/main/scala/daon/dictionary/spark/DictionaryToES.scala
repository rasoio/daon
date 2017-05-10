package daon.dictionary.spark

import org.apache.spark.sql._
import org.elasticsearch.spark.sql._

import scala.collection.mutable.ArrayBuffer

object DictionaryToES {

  def main(args: Array[String]) {


    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
      .config("es.nodes", "localhost")
      .config("es.port", "9200")
      .config("es.index.auto.create", "true")
      .getOrCreate()
    
    readJsonWriteEs(spark)

  }

  private def readJsonWriteEs(spark: SparkSession) = {

    
    import spark.implicits._

    val df = spark.read.json("/Users/mac/work/corpus/model/words.json")
//    df.createOrReplaceTempView("words")

    df.saveToEs("dictionary/words", Map("es.mapping.id" -> "seq"))

  }
}
