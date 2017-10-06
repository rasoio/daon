package daon.spark.write

import daon.spark.write.SejongSentences.addAlias
import org.apache.spark.sql._
import org.elasticsearch.spark.sql._

object UserSentences extends AbstractSentences {

  def main(args: Array[String]) {

    println(esNode, esPort)

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master(master)
      .config("es.nodes", esNode)
      .config("es.port", esPort)
      .config("es.index.auto.create", "false")
      .config("es.nodes.wan.only", "true") //only connects through the declared es.nodes
      .config("spark.ui.enabled", "false")
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

    addAlias(sentences, "sentences")
    addAlias(sentences, "train_sentences")
  }



  private def readUserJsonWriteEs(spark: SparkSession, jsonPath: String, trainIndexName: String, typeName: String) = {

//    val df = spark.read.json(jsonPath)
    val df = spark.read.format("org.apache.spark.sql.json").load(jsonPath)

    df.saveToEs(s"${trainIndexName}/${typeName}")
  }

}
