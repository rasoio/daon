package daon.spark.migration

import org.apache.commons.lang3.time.StopWatch
import org.apache.spark.sql._

import scala.collection.mutable.ArrayBuffer

object WordDataMig {

  case class Sentence(sentence: String, var eojeols: Seq[Eojeol] = ArrayBuffer[Eojeol]())

  case class Eojeol(seq: Long, var surface: String, var morphemes: Seq[Morpheme] = ArrayBuffer[Morpheme]())

  case class Morpheme(seq: Long, word: String, tag: String)

  // word 형식 => single word = "남성" or compound word = "남성의류:남성,의류"
  case class Words(word: String, weight: Int)

  def main(args: Array[String]) {

    val stopWatch = new StopWatch

    stopWatch.start()

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
//      .config("es.nodes", "localhost")
//      .config("es.port", "9200")
//      .config("es.index.auto.create", "true")
      .getOrCreate()


//    val df = spark.read.option("header", "true").csv("/Users/mac/Downloads/NIADic.csv")
//
//    val dic = df.toDF("term", "tag", "category")
//    dic.createOrReplaceTempView("dic")
//
//    dic.show()
//
//    val ncn = spark.sql(
//      """
//        select term, tag, max(category) as category
//        from dic
//        where tag = 'ncn'
//        group by term, tag
//      """
//    )
//
//    ncn.show()

    val df = spark.read.json("/Users/mac/work/corpus/new_words")

    val new_df = toDF(spark, df)


//    new_df.show(10, false)
//    new_df.saveToEs("niadic_sentences_v3/sentence")
    new_df.coalesce(1).write.format("org.apache.spark.sql.json").mode("overwrite").save("/Users/mac/work/corpus/new_words_results")

  }

  private def toDF(spark: SparkSession, df: Dataset[Row]): Dataset[Sentence] ={
    import spark.implicits._

    val new_df = df.map(row =>{
      val word = row.getAs[String]("word")
      val tag = row.getAs[String]("tag")
      val sentence = word
      val s = Sentence(sentence)

      val surface = word
      val ne = Eojeol(seq = 0, surface = surface)

      val nm = Morpheme(0, word, tag)

      ne.morphemes :+= nm
      s.eojeols :+= ne

      s
    })

    new_df
  }

}
