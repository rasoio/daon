package daon.spark.tags

import java.util

import daon.spark.{AbstractWriter, ManageJob, PreProcess}
import org.apache.spark.sql.{Dataset, _}
import org.elasticsearch.spark.sql._

import scala.collection.mutable.ArrayBuffer

object TagTrans extends AbstractWriter with ManageJob {

  case class TagTran(position: String, tag1: String, tag2: Option[String] = None, cost: Long)

  case class Tag(tag: String,
                 p_outer_tag: Option[String] = None,
                 n_outer_tag: Option[String] = None,
                 p_inner_tag: Option[String] = None,
                 n_inner_tag: Option[String] = None)

  val WEIGHT = 200

  val temp: ArrayBuffer[String] = ArrayBuffer[String]()


  def main(args: Array[String]) {

    val spark = getSparkSession()

    execute(spark)
  }

  override def getJobName() = "tag_trans"

  override def execute(spark: SparkSession): Unit = {
    val version = CONFIG.getString("index.tags.version")
    val scheme = CONFIG.getString("index.tags.scheme")
    val typeName = CONFIG.getString("index.tags.type")

    val indexName = s"tag_trans_$version"

    createIndex(indexName, scheme)

    val esDF = readESSentences(spark)

    val rawTags = toRawTags(spark, esDF)

    makeTagTransMap(spark, indexName, typeName)

    rawTags.unpersist()
    esDF.unpersist()

    addAlias(indexName, "tag_trans")
  }

  def makeTagTransMap(spark: SparkSession, indexName: String, typeName: String): Unit = {
    import spark.implicits._

    val tagTrans = ArrayBuffer[TagTran]()

    //태그별 노출 확률
    val tagsFreqMap = tagsFreq(spark)

    val broadcastVar = spark.sparkContext.broadcast(tagsFreqMap)
    val broadcastTagsFreqMap = broadcastVar.value

    //tag 전이 확률
    //1. 어절 시작 첫번째 노출 될 tag 확률
    firstTags(spark, tagTrans, broadcastTagsFreqMap)

    //2. 어절 중간 연결 확률
    middleTags(spark, tagTrans, broadcastTagsFreqMap)

    //3. 어절 마지막 노출 될 tag 확률
    lastTags(spark, tagTrans, broadcastTagsFreqMap)

    //4. 마지막 tag, 다음 어절 첫 tag
    connectTags(spark, tagTrans, broadcastTagsFreqMap)

    broadcastVar.destroy()

    val df = spark.sparkContext.parallelize(tagTrans).toDF()

//    df.write.format("org.apache.spark.sql.json").mode("overwrite").save("/Users/mac/work/corpus/tags")

    df.write.format("org.elasticsearch.spark.sql").mode("overwrite").save(s"$indexName/$typeName")
  }

  private def toRawTags(spark: SparkSession, esDF: Dataset[Row]): Dataset[Tag] ={
    import spark.implicits._

    val df = esDF.flatMap(row =>{

      val tags = ArrayBuffer[Tag]()

      val eojeols = row.getAs[Seq[Row]]("eojeols")

      val eHead = eojeols.head
      val elast = eojeols.last

      eojeols.indices.foreach(e=>{
        val eojeol = eojeols(e)

        var prevOuter = None : Option[String]
        var nextOuter = None : Option[String]

        if(eojeol != eHead){
          prevOuter = Option(getTag(eojeols(e-1).getAs[Seq[Row]]("morphemes").last))
        }

        if(eojeol != elast){
          nextOuter = Option(getTag(eojeols(e+1).getAs[Seq[Row]]("morphemes").head))
        }

        val morphemes = eojeol.getAs[Seq[Row]]("morphemes")

        val head = morphemes.head
        val last = morphemes.last

        morphemes.indices.foreach(m=>{
          val morpheme = morphemes(m)

          val tag = morpheme.getAs[String]("tag")

          var p_outer_tag = None : Option[String]

          if(morpheme == head && prevOuter.isDefined) {
            val p_outer = prevOuter.get
            p_outer_tag = Option(p_outer)
          }

          var n_outer_tag = None : Option[String]

          if(morpheme == last && nextOuter.isDefined) {
            val n_outer = nextOuter.get
            n_outer_tag = Option(n_outer)
          }

          var p_inner_tag = None : Option[String]

          if(morpheme != head){
            val prevInner = getTag(morphemes(m-1))
            p_inner_tag = Option(prevInner)
          }

          var n_inner_tag = None : Option[String]

          if(morpheme != last) {
            val nextInner = getTag(morphemes(m+1))
            n_inner_tag = Option(nextInner)
          }

          val nm = Tag(tag, p_outer_tag, n_outer_tag, p_inner_tag, n_inner_tag)

          tags += nm
        })

      })

      tags
    }).as[Tag]

    df.createOrReplaceTempView("tags")
    df.cache()

//    df.write.format("org.apache.spark.sql.json").mode("overwrite").save("/Users/mac/work/corpus/tags")

    df
  }


  def getTag(morpheme: Row): String = {
    val tag = morpheme.getAs[String]("tag")

    tag
  }


  private def tagsFreq(spark: SparkSession): Map[String, Long] = {
    val tagsDF = spark.sql(
      """
        select tag, count(*) as freq
        from tags
        group by tag
      """)

    tagsDF.collect().map(row => row.getAs[String]("tag") -> row.getAs[Long]("freq")).toMap
  }

  private def firstTags(spark: SparkSession, tagTrans: ArrayBuffer[TagTran], tagsFreqMap: Map[String, Long]): Unit ={
    val tagsDF = spark.sql(
      """
        select tag, count(*) as freq
        from tags
        where p_inner_tag is null
        group by tag
        order by count(*) desc
      """)

//    tagsDF.show(100)

    temp += "============= firstTag =============="

    tagsDF.collect().foreach(row => {
      val tag = row.getAs[String]("tag")
      val freq = row.getAs[Long]("freq")

      val maxFreq = getMaxFreq(tag, tagsFreqMap)
      val p = freq / maxFreq
      val cost = toCost(p)

      temp += s"$tag\t$freq\t$maxFreq\t$p\t$cost"

      tagTrans += TagTran("first", tag, None, cost)
    })
  }

  private def middleTags(spark: SparkSession, tagTrans: ArrayBuffer[TagTran], tagsFreqMap: Map[String, Long]): Unit = {
    val tagsDF = spark.sql(
      """
        select p_inner_tag as pTag, tag, count(*) as freq
        from tags
        where p_inner_tag is not null
        group by p_inner_tag, tag
        order by count(*) desc
      """)


    temp += "============= middleTag =============="

    tagsDF.collect().foreach(row => {
      val tag1 = row.getAs[String]("pTag")
      val tag2 = row.getAs[String]("tag")
      val freq = row.getAs[Long]("freq")

      val maxFreq = getMaxFreq(tag1, tagsFreqMap)
      val p = freq / maxFreq
      val cost = toCost(p)

      temp += s"$tag1\t$tag2\t$freq\t$maxFreq\t$p\t$cost"

      tagTrans += TagTran("middle", tag1, Option(tag2), cost)
    })

  }


  def lastTags(spark: SparkSession, tagTrans: ArrayBuffer[TagTran], tagsFreqMap: Map[String, Long]): Unit = {
    val tagsDF = spark.sql(
      """
        select tag, count(*) as freq
        from tags
        where n_inner_tag is null
        group by tag
        order by count(*) desc
      """)


    temp += "============= lastTag =============="

    tagsDF.collect().foreach(row => {
      val tag = row.getAs[String]("tag")
      val freq = row.getAs[Long]("freq")

      val maxFreq = getMaxFreq(tag, tagsFreqMap)
      val p = freq / maxFreq
      val cost = toCost(p)

      temp += s"$tag\t$freq\t$maxFreq\t$p\t$cost"

      tagTrans += TagTran("last", tag, None, cost)
    })

  }

  def connectTags(spark: SparkSession, tagTrans: ArrayBuffer[TagTran], tagsFreqMap: Map[String, Long]): Unit = {

     val tagsDF = spark.sql(
      """
        select tag, n_outer_tag as nTag, count(*) as freq
        from tags
        where n_inner_tag is null
        and n_outer_tag is not null
        group by tag, n_outer_tag
        order by count(*) desc
      """)


    temp += "============= connectTag =============="

    tagsDF.collect().foreach(row => {
      val tag1 = row.getAs[String]("tag")
      val tag2 = row.getAs[String]("nTag")
      val freq = row.getAs[Long]("freq")

      val maxFreq = getMaxFreq(tag1, tagsFreqMap)
      val p = freq / maxFreq
      val cost = toCost(p)

      temp += s"$tag1\t$tag2\t$freq\t$maxFreq\t$p\t$cost"

      tagTrans += TagTran("connect", tag1, Option(tag2), cost)
    })

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
