package daon.dictionary.spark

import java.{lang, util}

import daon.dictionary.spark.PreProcess.{Sentence, Word}
import org.apache.spark.sql._

import scala.math._
import scala.collection.mutable.ArrayBuffer

object MakeTagTrans {


  val temp: ArrayBuffer[String] = ArrayBuffer[String]()
  var tagsFreqMap: Map[String, Long] = Map[String, Long]()

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

    PreProcess.process(spark)

    makeTagTransMap(spark)

  }


  def makeTagTransMap(spark: SparkSession): util.Map[Integer, Integer] = {

    val tagTransMap = new util.HashMap[Integer, Integer]()

    tagsFreqMap = tagsFreq(spark)

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

  private def tagsFreq(spark: SparkSession): Map[String, Long] = {
    val tagsDF = spark.sql(
      """
        select tag, count(*) as freq
        from sentences
        group by tag
      """)

    tagsDF.collect().map(row => row.getAs[String]("tag") -> row.getAs[Long]("freq")).toMap
  }

  private def fistTag(spark: SparkSession, tagTransMap: util.Map[Integer, Integer]): Unit ={
    val tagsDF = spark.sql(
      """
        select 'FIRST' as pTag, tag, count(*) as freq
        from sentences
        where p_inner_tag is null
        group by tag
        order by count(*) desc
      """)

//    tagsDF.show(100)

    temp += "============= firstTag =============="

    tagsDF.collect().foreach(row => {
      val tag1 = row.getAs[String]("pTag")
      val tag2 = row.getAs[String]("tag")
      val freq = row.getAs[Long]("freq").toFloat

      val key = getKey(tag1, tag2)

      val maxFreq = getMaxFreq(tag2)
      val score = getScore(freq / maxFreq)

      temp += s"$tag1\t$tag2\t$freq\t$maxFreq\t$score"

      tagTransMap.put(key, score)
    })
  }

  private def middleTag(spark: SparkSession, tagTransMap: util.HashMap[Integer, Integer]): Unit = {
    val tagsDF = spark.sql(
      """
        select p_inner_tag as pTag, tag, count(*) as freq
        from sentences
        where p_inner_tag is not null
        group by p_inner_tag, tag
        order by count(*) desc
      """)


    temp += "============= middleTag =============="

    tagsDF.collect().foreach(row => {
      val tag1 = row.getAs[String]("pTag")
      val tag2 = row.getAs[String]("tag")
      val freq = row.getAs[Long]("freq").toFloat

      val key = getKey(tag1, tag2)

      val maxFreq = getMaxFreq(tag1)
      val score = getScore(freq / maxFreq)

      temp += s"$tag1\t$tag2\t$freq\t$maxFreq\t$score"

      tagTransMap.put(key, score)
    })
  }


  def lastTag(spark: SparkSession, tagTransMap: util.HashMap[Integer, Integer]): Unit = {
    val tagsDF = spark.sql(
      """
        select tag, 'LAST' as nTag, count(*) as freq
        from sentences
        where n_inner_tag is null
        group by tag
        order by count(*) desc
      """)


    temp += "============= lastTag =============="

    tagsDF.collect().foreach(row => {
      val tag1 = row.getAs[String]("tag")
      val tag2 = row.getAs[String]("nTag")
      val freq = row.getAs[Long]("freq").toFloat

      val key = getKey(tag1, tag2)

      val maxFreq = getMaxFreq(tag1)
      val score = getScore(freq / maxFreq)

      temp += s"$tag1\t$tag2\t$freq\t$maxFreq\t$score"

      tagTransMap.put(key, score)
    })
  }

  def connectTag(spark: SparkSession, tagTransMap: util.HashMap[Integer, Integer]): Unit = {

     val tagsDF = spark.sql(
      """
        select tag, n_outer_tag as nTag, count(*) as freq
        from sentences
        where n_inner_tag is null
        and n_outer_tag is not null
        group by tag, n_outer_tag
        order by count(*) desc
      """)


    temp += "============= connectTag =============="

    tagsDF.collect().foreach(row => {
      val tag1 = row.getAs[String]("tag")
      val tag2 = row.getAs[String]("nTag")
      val freq = row.getAs[Long]("freq").toFloat

      val key = getKey(tag1 + "|END", tag2)

      val maxFreq = getMaxFreq(tag1)
      val score = getScore(freq / maxFreq)

      temp += s"$tag1\t$tag2\t$freq\t$maxFreq\t$score"

      tagTransMap.put(key, score)
    })
  }

  def getMaxFreq(tag: String): Long = {

    tagsFreqMap(tag)
  }

  def getKey(prev: String, cur: String): Int = {

    val key = (prev + "|" + cur).hashCode

    key
  }

  def getScore(score: Float): Int = {
//    val value = sqrt(sqrt(sqrt(score))).toFloat
    val value = toCost(score)

    value
  }

  private def toCost(d: Float) = {
    val n = 200
    val score = Math.log(d)
    (-n * score).toShort
  }
}
