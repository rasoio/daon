package daon.spark

import java.util

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.httpclient.HttpStatus
import org.apache.http.HttpHost
import org.apache.http.client.methods.{HttpHead, HttpPost, HttpPut}
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.apache.spark.sql._
import org.elasticsearch.client.{Response, RestClient}
import org.elasticsearch.spark.sql._

trait AbstractWriter {

  val params: util.Map[String, String] = new util.HashMap[String, String]()

  val CONFIG : Config = {
    ConfigFactory.load("application.conf")
  }

  val master: String = CONFIG.getString("spark.master")
  val esNode: String = CONFIG.getString("spark.es.nodes")
  val esPort: Int = CONFIG.getInt("spark.es.port")
  val alias: String = CONFIG.getString("alias")


  val SENTENCES_INDEX_TYPE = "train_sentences/sentence"


  def getJobName(): String

  def getSparkSession(): SparkSession = {
    println(esNode, esPort)

    val spark = SparkSession
      .builder()
      .appName(getJobName())
      .master(master)
      .config("es.nodes", esNode)
      .config("es.port", esPort)
      .config("es.index.auto.create", "false")
      .config("es.nodes.wan.only", "true") //only connects through the declared es.nodes
      .config("spark.ui.enabled", "false")
      .getOrCreate()
    spark
  }

  def readESSentences(spark: SparkSession): Dataset[Row] = {
    // read from es
    //    val options = Map("es.read.field.exclude" -> "word_seqs")

    val esSentenceDF = spark.read.format("es").load(SENTENCES_INDEX_TYPE)
    //      .limit(1000)

    esSentenceDF.createOrReplaceTempView("es_sentence")
    esSentenceDF.cache()

    esSentenceDF
  }

  def createEsClient: RestClient = {
    RestClient.builder(new HttpHost(esNode, esPort)).build()
  }

  /**
    * 서버에 인덱스를 생성한다
    *
    * @param indexName
    */
  def deleteIndex(indexName: String): Boolean = {

    if(existsIndex(indexName)) {
      val restEsClient = createEsClient

      val res = restEsClient.performRequest(HttpHead.METHOD_NAME, indexName, params)

      val statusCode = res.getStatusLine.getStatusCode

      restEsClient.close()

      statusCode match {
        case HttpStatus.SC_OK => true
        case _ => false
      }
    }else{
      println(s"$indexName is not exist. ignore delete")

      true
    }
  }

  /**
    * 서버에 인덱스를 생성한다
    *
    * @param scheme
    */
  def createIndex(indexName: String, scheme: String): Boolean = {

    if(!existsIndex(indexName)) {
      val restEsClient = createEsClient

      val entity = new NStringEntity(scheme, ContentType.APPLICATION_JSON)

      val res: Response = restEsClient.performRequest(HttpPut.METHOD_NAME, indexName, params, entity)

      restEsClient.close()

      true
    }else{
      println(s"$indexName is already exist!!")

      false
    }
  }

  /**
    * alias 추가
    * @param indexName
    * @param aliasName
    * @return
    */
  def addAlias(indexName: String, aliasName: String): Boolean = {

    val data = alias.format(indexName, aliasName)

    val restEsClient = createEsClient

    val entity = new NStringEntity(data, ContentType.APPLICATION_JSON)

    val res: Response = restEsClient.performRequest(HttpPost.METHOD_NAME, "/_aliases", params, entity)

    restEsClient.close()

    true
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

  def writeToEs(df: DataFrame, indexName: String, typeName: String): Unit = {

    df.saveToEs(s"${indexName}/${typeName}")
  }

}
