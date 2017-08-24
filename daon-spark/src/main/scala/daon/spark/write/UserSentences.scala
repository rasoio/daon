package daon.spark.write

import org.apache.spark.sql._
import org.elasticsearch.spark.sql._

object UserSentences extends AbstractSentences {

  def main(args: Array[String]) {

    println(esNode, esPort)

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
      .config("es.nodes", esNode)
      .config("es.port", esPort)
      .config("es.index.auto.create", "false")
      .getOrCreate()

    val prefix = CONFIG.getString("index.prefix")

    val sentencesVersion = CONFIG.getString("index.sentences.version")
    val sentencesScheme = CONFIG.getString("index.sentences.scheme")
    val sentencesType = CONFIG.getString("index.sentences.type")
    val jsonPath = CONFIG.getString("index.sentences.jsonPath")

    val sentences = s"${prefix}_sentences_$sentencesVersion"

    val isNewCreate = createIndex(sentences, sentencesScheme)

    //append 방지
    if(isNewCreate){
      //초기 json 데이터 insert
      readUserJsonWriteEs(spark, jsonPath, sentences, sentencesType)
    }

  }

  private def readUserJsonWriteEs(spark: SparkSession, jsonPath: String, trainIndexName: String, typeName: String) = {

    val df = spark.read.json(jsonPath)

    df.saveToEs(s"${trainIndexName}/${typeName}")
  }

}
