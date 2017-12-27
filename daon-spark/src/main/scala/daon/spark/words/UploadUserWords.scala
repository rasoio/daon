package daon.spark.words
import java.io.InputStream
import java.util

import daon.core.data.{Morpheme, Word}
import daon.core.util.Utils
import daon.spark.AbstractWriter
import org.apache.spark.sql._

import scala.collection.mutable.ArrayBuffer

object UploadUserWords extends AbstractWriter {

  def main(args: Array[String]) {

    val spark = getSparkSession()

    val text =
      """
         하나 둘
         하나,하나/VV,1
         하나,하/NNG 만/NNB,1
      """

    import java.io.ByteArrayInputStream
    val input = new ByteArrayInputStream(text.getBytes())

    execute(spark, input, "test", isAppend = false)
  }

  override def getJobName() = "upload_user_words"

  def execute(spark: SparkSession, input: InputStream, prefix: String, isAppend: Boolean): util.List[String] = {
    import scala.collection.JavaConversions._

    val version = CONFIG.getString("index.words.version")
    val scheme = CONFIG.getString("index.words.scheme")
    val typeName = CONFIG.getString("index.words.type")

    val indexName = s"${prefix}_words_$version"

    createIndex(indexName, scheme)

    val rows = csvParse(input)

    val (df, errors) = readRows(spark, rows)

    val mode = if(isAppend) "append" else "overwrite"

    df.write.format("org.elasticsearch.spark.sql").mode(mode).save(s"$indexName/$typeName")

//    errors.foreach(println)

    addAlias(indexName, "words")

    errors
  }

  def csvParse(input: InputStream): util.List[Array[String]] = {
    import com.univocity.parsers.csv.CsvParserSettings
    val settings = new CsvParserSettings
    settings.getFormat.setLineSeparator("\n")

    import com.univocity.parsers.csv.CsvParser
    val parser = new CsvParser(settings)

    val rows = parser.parseAll(input)

    rows
  }


  def readRows(spark: SparkSession, rows: util.List[Array[String]]): (Dataset[Word], ArrayBuffer[String]) = {
    import spark.implicits._

    import scala.collection.JavaConversions._

    val words = ArrayBuffer[Word]()
    val errors = ArrayBuffer[String]()

    rows.indices.foreach(i => {
      val row = rows(i)
//      println(row.mkString(", "))

      val size = row.length

      try {
        size match {
          case 1 => {
            val surface = readSurface(row(0))
            val word = new Word(surface, ArrayBuffer(new Morpheme(surface, "NNG")), 1)

            words += word
          }
          case 2 => {
            val surface = readSurface(row(0))
            val weight = row(1).toInt // on error if not number
            val word = new Word(surface, ArrayBuffer(new Morpheme(surface, "NNG")), weight)

            words += word
          }
          case 3 => {
            val surface = readSurface(row(0))
            val morphemes = Utils.parseMorpheme(row(1)) // error if not parsed
            val weight = row(2).toInt // error if not number
            val word = new Word(surface, morphemes, weight)

            words += word
          }
          case _ => {
            throw new Exception("처리할수 없음")
          }
        }
      } catch {
        case e: Exception => {
          errors += s"$i 번째 row -> ${e.getMessage}"
        }
      }

    })

    implicit val WordEncoder: Encoder[Word] = Encoders.bean(classOf[Word])
    val df = words.toDF().as(WordEncoder)

    (df, errors)
  }

  def readSurface(str: String): String = {
    if(str.isEmpty){
      throw new Exception("surface 값이 없습니다.")
    }

    val trimmedStr = str.trim

    if("\\s".r.findAllIn(trimmedStr).nonEmpty){
      throw new Exception("surface 값에 공백이 들어있습니다.")
    }

    trimmedStr
  }

}
