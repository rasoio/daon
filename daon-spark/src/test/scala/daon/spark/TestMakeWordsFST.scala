package daon.spark

import java.io.InputStream
import java.util

import daon.core.data.{Eojeol, Word}
import daon.core.util.Utils
import daon.spark.PreProcess.Morpheme
import daon.spark.words.SentencesToWords
import org.junit.{Before, Test}
import org.scalatest.junit.JUnitSuite
import org.junit.Assert._

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class TestMakeWordsFST extends JUnitSuite{

  def csvParse(input: InputStream): util.List[Array[String]] = {
    import com.univocity.parsers.csv.CsvParserSettings
    val settings = new CsvParserSettings
    settings.getFormat.setLineSeparator("\n")
    settings.setHeaderExtractionEnabled(true)
    import com.univocity.parsers.csv.CsvParser
    val parser = new CsvParser(settings)

    val rows = parser.parseAll(input)

    val headers = parser.getContext.headers()
    rows
  }

  def check(eojeols: util.List[Eojeol], words: ArrayBuffer[Word]): Boolean =  {
    import scala.collection.JavaConversions._

//    println(eojeols.size, words.size)
    if(eojeols.size != words.size){
      return false
    }

    eojeols.indices.foreach(i=>{
      val eojeol = eojeols(i)
      val word = words(i)

//      println(eojeol.getSurface, word.getSurface)
      if(eojeol.getSurface != word.getSurface){
        return false
      }

      val a = eojeol.getMorphemes
      val b = word.getMorphemes

      if(a.size != b.size){
        return false
      }

      a.indices.foreach(j=>{
        val m1 = a(j)
        val m2 = b(j)

//        println(m1, m2, m1.hashCode, m1.hashCode)
        if(m1 != m2){
          return false
        }
      })
    })

    true
  }

  @Test
  def verifyPartialWords() {


    val filename = "testcase.csv"
    val stream : InputStream = getClass.getResourceAsStream(filename)


    import scala.collection.JavaConversions._

    val list = csvParse(stream)
    for (line <- list) {
      val surface = line(0)
      val morphemesText = line(1)
      val answer = line(2)
      val morphemes = Utils.parseMorpheme(morphemesText)
      val words = SentencesToWords.parsePartialWords(surface, morphemes)

      if(answer == null){
//        println("answer is null", words.length)
        assertTrue(words.isEmpty)
      }else{
        val text = answer.replace("||", "\n")
        val eojeols = Utils.parse(text)

        val checked = check(eojeols, words)

        if(!checked){
          println(eojeols, words, words.length, checked)
        }

        assertTrue(checked)
      }

//      println(surface, morphemes, answer, words)
    }

  }

}
