package daon.dictionary.spark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object SparkTest {
  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
      //      .config("spark.some.config.option", "some-value")
      .getOrCreate()

    val df = spark.read.json("/Users/mac/Downloads/sejong.json")

    df.createOrReplaceTempView("raw_sentence")

    // 구조를 이대로 해도 될까??
    val sqlDF = spark.sql(
      """
        | select sentence, surface,
        |        morpheme.word, morpheme.tag,
        |        morpheme.prevOuter.word as p_outer_word, morpheme.prevOuter.tag as p_outer_tag,
        |        morpheme.nextOuter.word as n_outer_word, morpheme.nextOuter.tag as n_outer_tag,
        |        morpheme.prevInner.word as p_inner_word, morpheme.prevInner.tag as p_inner_tag,
        |        morpheme.nextInner.word as n_inner_word, morpheme.nextInner.tag as n_inner_tag
        | from (
        |   select sentence, eojeol.surface as surface, eojeol.morphemes as morphemes
        |   from raw_sentence
        |   lateral view explode(eojeols) exploded_eojeols as eojeol
        | )
        | lateral view explode(morphemes) exploded_morphemes as morpheme
        |
      """.stripMargin)



    sqlDF.printSchema()

    //    sqlDF.show()


    sqlDF.createOrReplaceTempView("sentence")

    sqlDF.cache()

    //    22231026 ?
    //    22231621
    //총 노출 건수
    val totalCnt = spark.sql(
      """
        | select count(0) as cnt
        | from sentence
        | where tag not in ('nh', 'nb', 'un', 'ne')
      """.stripMargin)


    val cnt = totalCnt.first().getLong(0)

    println("totalCnt = " + cnt)

    val sqlDF2 = spark.sql(
      """
        | select word, tag, count(surface) as tf
        | from sentence
        | group by word, tag
        | order by count(surface) desc
      """.stripMargin)

    //    sqlDF.printSchema()


    val sqlDF3 = spark.sql(
      """
        | select tag, count(word) as tag_cnt
        | from sentence
        | group by tag
        | order by count(word) desc
      """.stripMargin)

    sqlDF3.collect().foreach(row => {

      val tag = row.get(0)
      val tagCnt = row.get(1)
      val where =
        s"""
           | tag = "${tag}"
         """.stripMargin

      //      val wordDF = sqlDF.where(where)

      //      val pInner = wordDF.groupBy("p_inner_tag").count().sort(desc("count"))

      println(tag, tagCnt)
      //      pInner.show()
      //      wordDF.show()

      //      println(row)
    })

    val count = sqlDF.count()

    println("group by Cnt = " + count)

    //    val sqlDF3 = sqlDF2.join(sqlDF, sqlDF("word") === sqlDF2("word") && sqlDF("tag") === sqlDF2("tag"), joinType = "inner")


    //    sqlDF3.show()


  }
}
