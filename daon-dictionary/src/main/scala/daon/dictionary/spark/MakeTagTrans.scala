package daon.dictionary.spark

import java.{lang, util}

import org.apache.spark.sql._
import scala.math._

import scala.collection.mutable.ArrayBuffer

object MakeTagTrans {


  val temp = ArrayBuffer[String]()

  def main(args: Array[String]) {


    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
//      .master("spark://daon.spark:7077")
      .config("es.nodes", "daon.es")
      .config("es.port", "9200")
      .config("es.index.auto.create", "true")
      .getOrCreate()

    MakeModel.readSentences(spark)

    MakeModel.createSentencesView(spark)

    makeTagTransMap(spark)

  }


  def makeTagTransMap(spark: SparkSession): util.Map[Integer, lang.Float] = {

    val tagTransMap = new util.HashMap[Integer, lang.Float]()


    //tag 전이 확률
    //1. 어절 시작 첫번째 노출 될 tag 확률
    fistTag(spark, tagTransMap)

    //2. 어절 중간 연결 확률
    middleTag(spark, tagTransMap)

    //3. 어절 마지막 노출 될 tag 확률
    lastTag(spark, tagTransMap)

    //4. 마지막 tag, 다음 어절 첫 tag
    connectTag(spark, tagTransMap)


    temp.foreach(println)

    tagTransMap
  }

  private def fistTag(spark: SparkSession, tagTransMap: util.Map[Integer, lang.Float]): Unit ={
    val tagsDF = spark.sql(
      """
        select 'FIRST' as pTag, tag, count(*) as freq
        from sentence
        where p_inner_tag is null
        group by tag
        order by count(*) desc
      """)

//    tagsDF.show(100)

    val maxFreq = tagsDF.groupBy().max("freq").collect()(0).getLong(0)

    temp += "============= firstTag =============="

    tagsDF.collect().foreach(row => {
      val pTag = row.getAs[String]("pTag")
      val tag = row.getAs[String]("tag")
      val freq = row.getAs[Long]("freq").toFloat

      val key = getKey(pTag, tag)

      val score = getScore(freq / maxFreq)

      temp += s"$pTag\t$tag\t$freq\t$maxFreq"

      tagTransMap.put(key, score)
    })
  }

  private def middleTag(spark: SparkSession, tagTransMap: util.HashMap[Integer, lang.Float]): Unit = {
    val tagsDF = spark.sql(
      """
        select p_inner_tag as pTag, tag, count(*) as freq
        from sentence
        where p_inner_tag is not null
        group by p_inner_tag, tag
        order by count(*) desc
      """)

    val maxFreq = tagsDF.groupBy().max("freq").collect()(0).getLong(0)

    temp += "============= middleTag =============="

    tagsDF.collect().foreach(row => {
      val pTag = row.getAs[String]("pTag")
      val tag = row.getAs[String]("tag")
      val freq = row.getAs[Long]("freq").toFloat

      val key = getKey(pTag, tag)

      val score = getScore(freq / maxFreq)

      temp += s"$pTag\t$tag\t$freq\t$maxFreq"

      tagTransMap.put(key, score)
    })
  }


  def lastTag(spark: SparkSession, tagTransMap: util.HashMap[Integer, lang.Float]): Unit = {
    val tagsDF = spark.sql(
      """
        select tag, 'LAST' as nTag, count(*) as freq
        from sentence
        where n_inner_tag is null
        group by n_inner_tag, tag
        order by count(*) desc
      """)

    val maxFreq = tagsDF.groupBy().max("freq").collect()(0).getLong(0)

    temp += "============= lastTag =============="

    tagsDF.collect().foreach(row => {
      val tag = row.getAs[String]("tag")
      val nTag = row.getAs[String]("nTag")
      val freq = row.getAs[Long]("freq").toFloat

      val key = getKey(tag, nTag)

      val score = getScore(freq / maxFreq)

      temp += s"$tag\t$nTag\t$freq\t$maxFreq"

      tagTransMap.put(key, score)
    })
  }

  def connectTag(spark: SparkSession, tagTransMap: util.HashMap[Integer, lang.Float]): Unit = {

     val tagsDF = spark.sql(
      """
        select tag, n_outer_tag as nTag, count(*) as freq
        from sentence
        where n_inner_tag is null
        and n_outer_tag is not null
        group by tag, n_outer_tag
        order by count(*) desc
      """)

    val maxFreq = tagsDF.groupBy().max("freq").collect()(0).getLong(0)

    temp += "============= connectTag =============="

    tagsDF.collect().foreach(row => {
      val tag = row.getAs[String]("tag") + "|END"
      val nTag = row.getAs[String]("nTag")
      val freq = row.getAs[Long]("freq").toFloat

      val key = getKey(tag, nTag)

      val score = getScore(freq / maxFreq)

      temp += s"$tag\t$nTag\t$freq\t$maxFreq"

      tagTransMap.put(key, score)
    })
  }


  def getKey(prev: String, cur: String): Int = {

    val key = (prev + "|" + cur).hashCode

    key
  }

  def getScore(score: Float): Float = {
    val value = sqrt(sqrt(sqrt(score))).toFloat

    value
  }
}
