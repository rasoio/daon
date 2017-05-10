package daon.dictionary.spark

import java.io.File
import java.util.regex.Pattern

import daon.dictionary.spark.SejongToJson.Morpheme
import org.apache.commons.io.FileUtils
import org.apache.spark.sql._
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

import scala.collection.mutable.ArrayBuffer

object IrregularJson {

  val filePath = "/Users/mac/work/corpus/sejong_utagger_irr.json"

  case class Irregular(surface: String, wordSeqs: Seq[Long])

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
      .getOrCreate()

    readJsonWriteIrrInfo(spark)

    val df = spark.read.json(filePath)

    df.show()
    df.createOrReplaceTempView("irr_set")

    var irrDF = spark.sql("""
      select surface, wordSeqs, count(*) as cnt
      from irr_set
      group by surface, wordSeqs
    """)

    irrDF.sort().coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/irr_info")

  }

  private def readJsonWriteIrrInfo(spark: SparkSession) = {

    val txtFile = new File(filePath)
    FileUtils.write(txtFile, "", "UTF-8")

    val df = spark.read.json("/Users/mac/work/corpus/sejong_utagger.json")

    df.show()
    df.printSchema()

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

//          if(surface == "나"){
//            println(irregular, surface_org, sentence, morphemes)
//          }

          implicit val formats = DefaultFormats
          val jsonString = write(irregular)

          FileUtils.write(txtFile, jsonString + System.lineSeparator(), "UTF-8", true)

        }
      })

    })

  }
}
