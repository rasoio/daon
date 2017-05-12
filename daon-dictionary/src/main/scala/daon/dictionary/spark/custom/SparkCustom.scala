package daon.dictionary.spark.custom

import java.io.File
import java.util.UUID
import java.util.regex.Pattern

import daon.analysis.ko.util.Utils
import daon.dictionary.spark.IrregularJson.Irregular
import daon.dictionary.spark.MakeEojeolToWords.Word
import daon.dictionary.spark.SejongToJson.Morpheme
import org.apache.commons.io.FileUtils
import org.apache.spark.sql._
import org.apache.spark.sql.functions.{col, udf}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}

object SparkCustom {

  val corpusFilePath = "/Users/mac/work/corpus/sejong_utagger.json"

  val irrFilePath = "/Users/mac/work/corpus/sejong_utagger_irr.json"

  case class Keyword(word: String, tag: String, tf: Long, prop: Double)


  def main(args: Array[String]) {


//    val spark = SparkSession
//      .builder()
//      .appName("daon dictionary")
//      .master("local[*]")
//      .getOrCreate()



    val str = "강남"
    val pos = "NNG"


//    val hash = Utils.hashCode(str + pos)
    val hash = new String(str + pos).hashCode

    println(hash)


//    val text = "이밖에 임야 및 공장·밭·논 등 산업 용지는 매년 줄어드는 반면 대지·종교용지 등은 늘어나는 등 도시개발이 지속되고 있으며 하루사이 4백97대의 차량이 증가하고 9백56건의 범죄가 발생하는 등 도시문제가 갈수록 심각해지고 있다.<강승규·김화균기자>"
    val text = "123"

    val results = text.split("[^가-힣]")

    println(results.size)
    results.foreach(println)


    val words = ArrayBuffer[String]()

    words += ("나이키", "아디다스")
    words += "아디다스"

    println(words)

    val wordSeqs = ArrayBuffer[Long]()

    breakable {
      for (i <- 0 to (10 - 1)) {

        if (i > 5) {
          break
        }

        wordSeqs += i
      }
    }

    println(wordSeqs)

    //    val replaceHashTag = udf[String, String]( _.replaceAll("[#$&\\.\\,\"\']", "").toLowerCase )
//    val tagJDBC = sqlContext.read.format("jdbc").options(Map(
//      "dbtable" -> "new_pikicast_common.TAG",
//      "numPartitions" -> "30") ++ mysqlConInfo).load.select("tag_id", "title")
//      .withColumn("title", replaceHashTag(col("title")))

//    readJsonWriteParquet(spark)

  }








  private def readJsonWriteParquet(spark: SparkSession) = {

    val df = spark.read.json(corpusFilePath)
    df.createOrReplaceTempView("raw_sentence")




    //불규칙 사전
    val txtFile = new File(irrFilePath)
    FileUtils.write(txtFile, "", "UTF-8")

    df.foreach(row=>{
      val sentence = row.getAs[String]("sentence")
      val eojeols = row.getAs[Seq[Row]]("eojeols")


      eojeols.indices.foreach(e=>{
        val eojeol = eojeols(e)

        val surface_org = eojeol.getAs[String]("surface")
        val morphemes = eojeol.getAs[Seq[Row]]("morphemes")

        var surface = surface_org

        val irrArr = ArrayBuffer[Morpheme]()

        morphemes.indices.foreach(m=>{
          val morpheme = morphemes(m)

          val seq = morpheme.getAs[Long]("seq")
          val word = morpheme.getAs[String]("word")
          val tag = morpheme.getAs[String]("tag")

          val irrMorpheme = Morpheme(seq = seq, word = word, tag = tag)

          if(!surface.contains(word)){
            irrArr += irrMorpheme
          }

          surface = surface.replaceFirst(Pattern.quote(word), "")

        })

        if(surface.length > 0 && irrArr.nonEmpty){

          val irregular = Irregular(surface, irrArr.map(m=>m.seq))

          implicit val formats = DefaultFormats
          val jsonString = write(irregular)

          FileUtils.write(txtFile, jsonString + System.lineSeparator(), "UTF-8", true)

        }
      })

    })


    // 구조를 이대로 해도 될까??
    val allDF = spark.sql(
      """
        | SELECT
        |        sentence,
        |        seq as eojeol_seq,
        |        offset as eojeol_offset,
        |        surface,
        |        morpheme.seq as word_seq,
        |        morpheme.word,
        |        morpheme.tag,
        |        morpheme.prevOuter.seq as p_outer_seq,
        |        morpheme.prevOuter.word as p_outer_word,
        |        morpheme.prevOuter.tag as p_outer_tag,
        |        morpheme.nextOuter.seq as n_outer_seq,
        |        morpheme.nextOuter.word as n_outer_word,
        |        morpheme.nextOuter.tag as n_outer_tag,
        |        morpheme.prevInner.seq as p_inner_seq,
        |        morpheme.prevInner.word as p_inner_word,
        |        morpheme.prevInner.tag as p_inner_tag,
        |        morpheme.nextInner.seq as n_inner_seq,
        |        morpheme.nextInner.word as n_inner_word,
        |        morpheme.nextInner.tag as n_inner_tag
        | FROM (
        |   SELECT sentence, eojeol.surface as surface, eojeol.seq, eojeol.offset, eojeol.morphemes as morphemes
        |   FROM raw_sentence
        |   LATERAL VIEW explode(eojeols) exploded_eojeols as eojeol
        | )
        | LATERAL VIEW explode(morphemes) exploded_morphemes as morpheme
        |
      """.stripMargin)

    allDF.printSchema()

    val count = allDF.count()
    println("all cnt = " + count)

    //    allDF.registerTempTable("sentence")
    allDF.createOrReplaceTempView("sentence")

    allDF.show()

//    allDF.write.mode(SaveMode.Overwrite).parquet("/Users/mac/work/corpus/utagger_all")

  }
}
