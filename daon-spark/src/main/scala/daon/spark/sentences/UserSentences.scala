package daon.spark.sentences

import daon.spark.AbstractWriter
import org.apache.spark.sql._
import org.elasticsearch.spark.sql._

object UserSentences extends AbstractWriter {

  def main(args: Array[String]) {

    println(esNode, esPort)

    val spark = getSparkSession()

    val prefix = CONFIG.getString("index.prefix")
    val jsonPath = CONFIG.getString("index.jsonPath")

    val sentencesVersion = CONFIG.getString("index.sentences.version")
    val sentencesScheme = CONFIG.getString("index.sentences.scheme")
    val sentencesType = CONFIG.getString("index.sentences.type")

    val sentences = s"${prefix}_sentences_$sentencesVersion"

    val isNewCreate = createIndex(sentences, sentencesScheme)

    //append 방지
    if(isNewCreate){
      //초기 json 데이터 insert
      readUserJsonWriteEs(spark, jsonPath, sentences, sentencesType)
    }

    addAlias(sentences, "sentences")
    addAlias(sentences, "train_sentences")
  }

  override def getJobName() = "user_sentences_write"



  private def readUserJsonWriteEs(spark: SparkSession, jsonPath: String, indexName: String, typeName: String): Unit = {

//    val df = spark.read.json(jsonPath)
    val df = spark.read.format("org.apache.spark.sql.json").load(jsonPath)


    writeToEs(df, indexName, typeName)
  }

}
