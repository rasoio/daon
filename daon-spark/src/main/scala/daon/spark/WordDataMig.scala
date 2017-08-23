package daon.spark

import java.io.FileOutputStream
import java.util
import java.util.Collections

import daon.analysis.ko.fst.DaonFSTBuilder
import daon.analysis.ko.model._
import daon.analysis.ko.proto.Model
import daon.spark.SentenceDataMig.{Eojeol, Morpheme, Sentence, replace}
import org.apache.commons.lang3.time.StopWatch
import org.apache.spark.sql._

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}
import org.elasticsearch.spark.sql._

object WordDataMig {

  def main(args: Array[String]) {

    val stopWatch = new StopWatch

    stopWatch.start()

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
      .config("es.nodes", "localhost")
      .config("es.port", "9200")
      .config("es.index.auto.create", "true")
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

    val df = spark.read.json("/Users/mac/work/corpus/NIADic_words")

    val new_df = toDF(spark, df)


//    new_df.show(10, false)
    new_df.saveToEs("niadic_sentences_v3/sentence")

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
