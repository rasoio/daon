package daon.dictionary.spark

import java.{lang, util}

import daon.dictionary.spark.PreProcess.{Sentence, Word}
import org.apache.spark.sql._

import scala.math._
import scala.collection.mutable.ArrayBuffer

object MakeTagTrans {


  val WEIGHT = 200

  val temp: ArrayBuffer[String] = ArrayBuffer[String]()
  var tagsFreqMap: Map[String, Long] = Map[String, Long]()

  case class TagTrans(firstTags: util.ArrayList[String], middleTags: util.ArrayList[String],
                      lastTags: util.ArrayList[String], connectTags: util.ArrayList[String])

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


  def makeTagTransMap(spark: SparkSession): TagTrans = {

    val tagTransMap = new util.HashMap[Integer, Integer]()

    //태그별 노출 확률
    tagsFreqMap = tagsFreq(spark)

    //tag 전이 확률
    //1. 어절 시작 첫번째 노출 될 tag 확률
    val first = fistTags(spark, tagTransMap)

    //2. 어절 중간 연결 확률
    val middle = middleTags(spark, tagTransMap)

    //3. 어절 마지막 노출 될 tag 확률
    val last = lastTags(spark, tagTransMap)

    //4. 마지막 tag, 다음 어절 첫 tag
    val connect = connectTags(spark, tagTransMap)

    temp.foreach(println)

    TagTrans(first, middle, last, connect)
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

  private def fistTags(spark: SparkSession, tagTransMap: util.Map[Integer, Integer]): util.ArrayList[String] ={
    val tagsDF = spark.sql(
      """
        select tag, count(*) as freq
        from sentences
        where p_inner_tag is null
        group by tag
        order by count(*) desc
      """)

//    tagsDF.show(100)

    temp += "============= firstTag =============="

    val tags = new util.ArrayList[String]

    tagsDF.collect().foreach(row => {
      val tag = row.getAs[String]("tag")
      val freq = row.getAs[Long]("freq").toFloat

      val maxFreq = getMaxFreq(tag)
      val score = getScore(freq / maxFreq)

      temp += s"$tag\t$freq\t$maxFreq\t$score"

      tags.add(s"$tag,$score")
    })

    tags
  }

  private def middleTags(spark: SparkSession, tagTransMap: util.HashMap[Integer, Integer]): util.ArrayList[String] = {
    val tagsDF = spark.sql(
      """
        select p_inner_tag as pTag, tag, count(*) as freq
        from sentences
        where p_inner_tag is not null
        group by p_inner_tag, tag
        order by count(*) desc
      """)


    temp += "============= middleTag =============="

    val tags = new util.ArrayList[String]

    tagsDF.collect().foreach(row => {
      val tag1 = row.getAs[String]("pTag")
      val tag2 = row.getAs[String]("tag")
      val freq = row.getAs[Long]("freq").toFloat

      val maxFreq = getMaxFreq(tag1)
      val score = getScore(freq / maxFreq)

      temp += s"$tag1\t$tag2\t$freq\t$maxFreq\t$score"

      tags.add(s"$tag1,$tag2,$score")
    })

    tags
  }


  def lastTags(spark: SparkSession, tagTransMap: util.HashMap[Integer, Integer]): util.ArrayList[String] = {
    val tagsDF = spark.sql(
      """
        select tag, count(*) as freq
        from sentences
        where n_inner_tag is null
        group by tag
        order by count(*) desc
      """)


    temp += "============= lastTag =============="

    val tags = new util.ArrayList[String]

    tagsDF.collect().foreach(row => {
      val tag = row.getAs[String]("tag")
      val freq = row.getAs[Long]("freq").toFloat

      val maxFreq = getMaxFreq(tag)
      val score = getScore(freq / maxFreq)

      temp += s"$tag\t$freq\t$maxFreq\t$score"

      tags.add(s"$tag,$score")
    })

    tags
  }

  def connectTags(spark: SparkSession, tagTransMap: util.HashMap[Integer, Integer]): util.ArrayList[String] = {

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

    val tags = new util.ArrayList[String]

    tagsDF.collect().foreach(row => {
      val tag1 = row.getAs[String]("tag")
      val tag2 = row.getAs[String]("nTag")
      val freq = row.getAs[Long]("freq").toFloat

      val maxFreq = getMaxFreq(tag1)
      val score = getScore(freq / maxFreq)

      temp += s"$tag1\t$tag2\t$freq\t$maxFreq\t$score"

      tags.add(s"$tag1,$tag2,$score")
    })

    tags
  }

  def getMaxFreq(tag: String): Long = {

    tagsFreqMap(tag)
  }

  def getScore(score: Float): Int = {
    val value = toCost(score)

    value
  }

  private def toCost(d: Float) = {
    val n = WEIGHT
    val score = Math.log(d)
    (-n * score).toShort
  }
}
