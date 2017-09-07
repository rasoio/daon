package daon.spark

import java.util

import daon.spark.PreProcess.Sentence
import org.apache.spark.sql.{Dataset, _}

import scala.collection.mutable.ArrayBuffer

object MakeTagTrans {


  val WEIGHT = 200

  val temp: ArrayBuffer[String] = ArrayBuffer[String]()

  case class TagTrans(firstTags: util.ArrayList[String], middleTags: util.ArrayList[String],
                      lastTags: util.ArrayList[String], connectTags: util.ArrayList[String])

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
//      .master("spark://daon.spark:7077")
      .config("es.nodes", "localhost")
      .config("es.port", "9200")
      .getOrCreate()

    val processedData = PreProcess.process(spark)
    val sentences: Dataset[Row] = processedData.sentences

    makeTagTransMap(spark, sentences)

  }

  def makeTagTransMap(spark: SparkSession, sentences: Dataset[Row]): TagTrans = {

    val tagTransMap = new util.HashMap[Integer, Integer]()

    //태그별 노출 확률
    val tagsFreqMap = tagsFreq(spark)

    val broadcastVar = spark.sparkContext.broadcast(tagsFreqMap)
    val broadcastTagsFreqMap = broadcastVar.value

    //tag 전이 확률
    //1. 어절 시작 첫번째 노출 될 tag 확률
    val first = fistTags(spark, tagTransMap, broadcastTagsFreqMap)

    //2. 어절 중간 연결 확률
    val middle = middleTags(spark, tagTransMap, broadcastTagsFreqMap)

    //3. 어절 마지막 노출 될 tag 확률
    val last = lastTags(spark, tagTransMap, broadcastTagsFreqMap)

    //4. 마지막 tag, 다음 어절 첫 tag
    val connect = connectTags(spark, tagTransMap, broadcastTagsFreqMap)

//    temp.foreach(println)

    broadcastVar.destroy()
    sentences.unpersist()

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

  private def fistTags(spark: SparkSession, tagTransMap: util.Map[Integer, Integer], tagsFreqMap: Map[String, Long]): util.ArrayList[String] ={
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
      val freq = row.getAs[Long]("freq")

      val maxFreq = getMaxFreq(tag, tagsFreqMap)
      val p = freq / maxFreq
      val cost = toCost(p)

      temp += s"$tag\t$freq\t$maxFreq\t$p\t$cost"

      tags.add(s"$tag,$cost")
    })

    tags
  }

  private def middleTags(spark: SparkSession, tagTransMap: util.HashMap[Integer, Integer], tagsFreqMap: Map[String, Long]): util.ArrayList[String] = {
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
      val freq = row.getAs[Long]("freq")

      val maxFreq = getMaxFreq(tag1, tagsFreqMap)
      val p = freq / maxFreq
      val cost = toCost(p)

      temp += s"$tag1\t$tag2\t$freq\t$maxFreq\t$p\t$cost"

      tags.add(s"$tag1,$tag2,$cost")
    })

    tags
  }


  def lastTags(spark: SparkSession, tagTransMap: util.HashMap[Integer, Integer], tagsFreqMap: Map[String, Long]): util.ArrayList[String] = {
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
      val freq = row.getAs[Long]("freq")

      val maxFreq = getMaxFreq(tag, tagsFreqMap)
      val p = freq / maxFreq
      val cost = toCost(p)

      temp += s"$tag\t$freq\t$maxFreq\t$p\t$cost"

      tags.add(s"$tag,$cost")
    })

    tags
  }

  def connectTags(spark: SparkSession, tagTransMap: util.HashMap[Integer, Integer], tagsFreqMap: Map[String, Long]): util.ArrayList[String] = {

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
      val freq = row.getAs[Long]("freq")

      val maxFreq = getMaxFreq(tag1, tagsFreqMap)
      val p = freq / maxFreq
      val cost = toCost(p)

      temp += s"$tag1\t$tag2\t$freq\t$maxFreq\t$p\t$cost"

      tags.add(s"$tag1,$tag2,$cost")
    })

    tags
  }

  def getMaxFreq(tag: String, tagsFreqMap: Map[String, Long]): Float = {

    tagsFreqMap(tag).toFloat
  }

  private def toCost(p: Float) = {
    val w = WEIGHT
    val score = Math.log(p)
    (-w * score).toShort
  }
}
