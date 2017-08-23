package daon.spark

import java.util

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.httpclient.HttpStatus
import org.apache.http.HttpHost
import org.apache.http.client.methods.{HttpHead, HttpPut}
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.apache.spark.sql._
import org.elasticsearch.client.{Response, RestClient}
import org.elasticsearch.spark.sql._

object InitSejongSentences {

  val params: util.Map[String, String] = new util.HashMap[String, String]()

  val CONFIG : Config = {
    ConfigFactory.load("application.conf")
  }

  val esNode: String = CONFIG.getString("spark.es.nodes")
  val esPort: Int = CONFIG.getInt("spark.es.port")

  def createEsClient: RestClient = {
    RestClient.builder(new HttpHost(esNode, esPort)).build()
  }

  def main(args: Array[String]) {

//    println(esNode, esPort)

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
//      .master("spark://daon.spark:7077")
      .config("es.nodes", esNode)
      .config("es.port", esPort)
      .config("es.index.auto.create", "false")
      .getOrCreate()

    val prefix = CONFIG.getString("index.prefix")

    val sentencesVersion = CONFIG.getString("index.sentences.version")
    val sentencesScheme = CONFIG.getString("index.sentences.scheme")
    val sentencesType = CONFIG.getString("index.sentences.type")
    val jsonPath = CONFIG.getString("index.sentences.jsonPath")

    val trainSentences = s"${prefix}_train_sentences_$sentencesVersion"
    val testSentences = s"${prefix}_test_sentences_$sentencesVersion"

    createIndex(trainSentences, sentencesScheme)
    createIndex(testSentences, sentencesScheme)

    //초기 json 데이터 insert
    readJsonWriteEs(spark, jsonPath, trainSentences, testSentences, sentencesType)


    val modelsVersion = CONFIG.getString("index.models.version")
    val modelsScheme = CONFIG.getString("index.models.scheme")

    val models = s"models_$modelsVersion"

    createIndex(models, modelsScheme)
  }


  private def readJsonWriteEs(spark: SparkSession, jsonPath: String, trainIndexName: String, testIndexName: String, typeName: String) = {

    val df = spark.read.json(jsonPath)

    // 9:1
    val splitDF = df.randomSplit(Array(0.9, 0.1))
    val trainDF = splitDF(0)
    val testDF = splitDF(1)

//    println("sentences total cnt : " + cnt)
//    println("train total cnt : " + trainDF.count())
//    println("test total cnt : " + testDF.count())

    trainDF.saveToEs(s"${trainIndexName}/${typeName}")
    testDF.saveToEs(s"${testIndexName}/${typeName}")

  }


  /**
    * 서버에 인덱스를 생성한다
    *
    * @param scheme
    */
  def createIndex(indexName: String, scheme: String): Unit = {
    
    if(!existsIndex(indexName)) {
      val restEsClient = createEsClient

      val entity = new NStringEntity(
        scheme
        , ContentType.APPLICATION_JSON)

      val res: Response = restEsClient.performRequest(HttpPut.METHOD_NAME, indexName, params, entity)
//      println(res.getEntity.getContent)

      restEsClient.close()
    }else{
      println(s"$indexName is already exist!!")
    }
  }

  /**
    * 서버에 해당하는 이름의 index가 생성되어있는지 체크한다.
    * @param indexName
    * @return 생성유무
    */
  def existsIndex(indexName:String ): Boolean = {
    // spec https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-exists.html
    val restClient = createEsClient

    val res = restClient.performRequest(HttpHead.METHOD_NAME, indexName, params)

    val statusCode = res.getStatusLine.getStatusCode

    restClient.close()

    statusCode match {
      case HttpStatus.SC_OK => true
      case _ => false
    }
  }

}
