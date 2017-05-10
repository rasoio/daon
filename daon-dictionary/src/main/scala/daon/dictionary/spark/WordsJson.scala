package daon.dictionary.spark

import java.io.File

import daon.dictionary.spark.SejongToJson.Morpheme
import org.apache.commons.io.FileUtils
import org.apache.spark.sql._
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

import scala.collection.mutable.ArrayBuffer

object WordsJson {


//  {"seq":1,"word":"!","tag":"sf","irrRule":null,"prob":7.3839235,"subWords":null,"desc":""}
  case class Irregular(surface: String, wordSeqs: Seq[Long])


  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
      .getOrCreate()



    readJsonWriteParquet(spark)

  }

//  .AnalysisException: cannot resolve '`eojeols`' due to data type mismatch: cannot cast
//
//      ArrayType(StructType(
//        StructField(morphemes,
//          ArrayType(StructType(
//            StructField(nextInner,StructType(StructField(seq,LongType,true), StructField(tag,StringType,true), StructField(word,StringType,true)),true),
//            StructField(nextOuter,StructType(StructField(seq,LongType,true), StructField(tag,StringType,true), StructField(word,StringType,true)),true),
//            StructField(prevInner,StructType(StructField(seq,LongType,true), StructField(tag,StringType,true), StructField(word,StringType,true)),true),
//            StructField(prevOuter,StructType(StructField(seq,LongType,true), StructField(tag,StringType,true), StructField(word,StringType,true)),true),
//            StructField(seq,LongType,true),
//            StructField(tag,StringType,true),
//            StructField(word,StringType,true)
//          ),true),true),
//        StructField(offset,LongType,true),
//        StructField(seq,LongType,true),
//        StructField(surface,StringType,true)
//      ),true)
//
//
//      ArrayType(StructType(
//        StructField(morphemes,
//          ArrayType(StructType(
//            StructField(nextInner,StructType(StructField(seq,LongType,true), StructField(word,StringType,true), StructField(tag,StringType,true)),true),
//            StructField(nextOuter,StructType(StructField(seq,LongType,true), StructField(word,StringType,true), StructField(tag,StringType,true)),true),
//            StructField(prevInner,StructType(StructField(seq,LongType,true), StructField(word,StringType,true), StructField(tag,StringType,true)),true),
//            StructField(prevOuter,StructType(StructField(seq,LongType,true), StructField(word,StringType,true), StructField(tag,StringType,true)),true),
//            StructField(seq,LongType,true),
//            StructField(tag,StringType,true),
//            StructField(word,StringType,true)
//          ),true),true),
//        StructField(offset,LongType,true),
//        StructField(seq,LongType,true),
//        StructField(surface,StringType,true)
//      ),true)

  private def readJsonWriteParquet(spark: SparkSession) = {

    val txtFile = new File("/Users/mac/work/corpus/sejong_utagger_irr.json")
    FileUtils.write(txtFile, "", "UTF-8")

//        val df = spark.read.json("/Users/mac/work/corpus/sejong_utagger_mini.json").as[Sentence]
    val df = spark.read.json("/Users/mac/work/corpus/sejong_utagger.json")

    df.show()
    df.printSchema()

//    val result = scala.collection.mutable.Map[String, Long]()
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

          surface = surface.replace(word, "")

        })


        if(surface.length > 0 && irrArr.nonEmpty){

//          val key = surface + "|" + irrArr.map(m=>m.seq).mkString(",")

          val irregular = Irregular(surface, irrArr.map(m=>m.seq))

          implicit val formats = DefaultFormats
          val jsonString = write(irregular)

          FileUtils.write(txtFile, jsonString + System.lineSeparator(), "UTF-8", true)

//          println(key)
//          println(surface_org, surface, irrArr)
        }
      })

    })

  }
}
