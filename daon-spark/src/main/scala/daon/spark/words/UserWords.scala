package daon.spark.words

import daon.spark.AbstractWriter
import org.apache.spark.sql._

object UserWords extends AbstractWriter {

  def main(args: Array[String]) {

    println(esNode, esPort)

    val spark = getSparkSession()

    val prefix = CONFIG.getString("index.prefix")
    val jsonPath = CONFIG.getString("index.jsonPath")

    val wordsVersion = CONFIG.getString("index.words.version")
    val wordsScheme = CONFIG.getString("index.words.scheme")
    val wordsType = CONFIG.getString("index.words.type")

    val words = s"${prefix}_words_$wordsVersion"

    val isNewCreate = createIndex(words, wordsScheme)

    //append 방지
    if(isNewCreate){
      //초기 json 데이터 insert
      readUserJsonWriteEs(spark, jsonPath, words, wordsType)
    }

    addAlias(words, "words")
  }


  override def getJobName() = "user_words"

  private def readUserJsonWriteEs(spark: SparkSession, jsonPath: String, indexName: String, typeName: String): Unit = {

//    val df = spark.read.json(jsonPath)
    val df = spark.read.format("org.apache.spark.sql.json").load(jsonPath)

    writeToEs(df, indexName, typeName)
  }

}
