package daon.dictionary.spark

import daon.dictionary.spark.CorpusToES.{Eojeol, Morpheme, Sentence}
import org.apache.spark.sql._
import org.elasticsearch.spark.sql._

import scala.collection.mutable.ArrayBuffer

object SparkMakeModel {

  def main(args: Array[String]) {


    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
      .config("es.nodes", "localhost")
      .config("es.port", "9200")
      .config("es.index.auto.create", "true")
      .getOrCreate()

    readEs(spark)

  }


  case class Sentence(sentence: String, eojeols: ArrayBuffer[Eojeol] = ArrayBuffer[Eojeol](), word_seqs: ArrayBuffer[Long] = ArrayBuffer[Long]())

  case class Eojeol(seq: Long, surface: String, offset: Long, morphemes: ArrayBuffer[Morpheme] = ArrayBuffer[Morpheme]())

  case class Morpheme(seq: Long, word: String, tag: String,
                      p_outer_seq: Option[Long] = None, p_outer_word: Option[String] = None, p_outer_tag: Option[String] = None,
                      n_outer_seq: Option[Long] = None, n_outer_word: Option[String] = None, n_outer_tag: Option[String] = None,
                      p_inner_seq: Option[Long] = None, p_inner_word: Option[String] = None, p_inner_tag: Option[String] = None,
                      n_inner_seq: Option[Long] = None, n_inner_word: Option[String] = None, n_inner_tag: Option[String] = None
                      )


  private def readEs(spark: SparkSession) = {
    import spark.implicits._

    val options = Map("es.read.field.exclude" -> "sentence,word_seqs")


    val df = spark.read.format("es").options(options).load("corpus/sentences")

    df.show(10)

    df.printSchema()

    df.createOrReplaceTempView("raw_sentence")

    val sentence_df = spark.sql(
      """
        | SELECT
        |        seq as eojeol_seq,
        |        offset as eojeol_offset,
        |        surface,
        |        morpheme.seq as word_seq,
        |        morpheme.word,
        |        morpheme.tag,
        |        morpheme.p_outer_seq as p_outer_seq,
        |        morpheme.p_outer_word as p_outer_word,
        |        morpheme.p_outer_tag as p_outer_tag,
        |        morpheme.n_outer_seq as n_outer_seq,
        |        morpheme.n_outer_word as n_outer_word,
        |        morpheme.n_outer_tag as n_outer_tag,
        |        morpheme.p_inner_seq as p_inner_seq,
        |        morpheme.p_inner_word as p_inner_word,
        |        morpheme.p_inner_tag as p_inner_tag,
        |        morpheme.n_inner_seq as n_inner_seq,
        |        morpheme.n_inner_word as n_inner_word,
        |        morpheme.n_inner_tag as n_inner_tag
        | FROM (
        |   SELECT eojeol.surface as surface, eojeol.seq, eojeol.offset, eojeol.morphemes as morphemes
        |   FROM raw_sentence
        |   LATERAL VIEW explode(eojeols) exploded_eojeols as eojeol
        | )
        | LATERAL VIEW explode(morphemes) exploded_morphemes as morpheme
        |
      """.stripMargin)

    sentence_df.createOrReplaceTempView("sentence")
    sentence_df.cache()

    sentence_df.printSchema()

    sentence_df.take(10).foreach(println)


    //첫 tag 확률
    val tagFreqDF = spark.sql(
      """
        select tag, count(*) as cnt
        from sentence
        where p_inner_tag is null
        group by tag
        order by count(*) desc
      """)

    //tag 전이 확률
    val tagTransFreqDF = spark.sql(
      """
        select tag, n_inner_tag as nInnerTag, count(*) as cnt
        from sentence
        where n_inner_tag is not null
        group by tag, n_inner_tag
        order by count(*) desc
      """)

    tagFreqDF.sort().coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/tag")
    tagTransFreqDF.sort().coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/tag_trans")


    //inner 연결 정보 모델

    val innerTransFreqDF = spark.sql(
      """
        select
            word_seq as wordSeq,
            n_inner_seq as nInnerSeq,
            count(*) as cnt
        from sentence
        where word_seq > 0
        and n_inner_seq > 0
        and tag not in ('SS','SP','SN','SH','SL','SW','SE','SO')
        and n_inner_tag not in ('SS','SP','SN','SH','SL','SW','SE','SO')
        group by  word_seq, n_inner_seq
      """)

    innerTransFreqDF.coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/inner_info")


    val outerTransFreqDF = spark.sql(
      """
        select
            p_outer_seq as pOuterSeq,
            word_seq as wordSeq,
            count(*) as cnt
        from sentence
        where p_outer_seq > 0
        and  word_seq > 0
        and tag not in ('SS','SP','SN','SH','SL','SW','SE','SO')
        and p_outer_tag not in ('SS','SP','SN','SH','SL','SW','SE','SO')
        group by p_outer_seq, word_seq
      """)

    outerTransFreqDF.coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/outer_info")

  }
}
