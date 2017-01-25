package daon.dictionary.spark

import org.apache.spark.sql.SparkSession

object SparkTest {
  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
//      .config("spark.some.config.option", "some-value")
      .getOrCreate()

    val df = spark.read.json("/Users/mac/Downloads/sejong.json")

    df.createOrReplaceTempView("sentence")

    // 구조를 이대로 해도 될까??
    val sqlDF = spark.sql(
      """
        | select sentence, surface, morpheme.word, morpheme.tag
        | from (
        |   select sentence, eojeol.surface as surface, eojeol.morphemes as morphemes
        |   from sentence
        |   lateral view explode(eojeols) exploded_eojeols as eojeol
        | )
        | lateral view explode(morphemes) exploded_morphemes as morpheme
        |
      """.stripMargin)

    sqlDF.printSchema()

    sqlDF.show()


  }
}
