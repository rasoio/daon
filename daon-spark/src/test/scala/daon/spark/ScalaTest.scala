package daon.spark

import java.io.{ByteArrayOutputStream, FileInputStream}

import com.google.protobuf.CodedInputStream
import daon.analysis.ko.proto.Model
import org.apache.commons.io.IOUtils
import org.apache.spark.sql.SparkSession
import org.elasticsearch.spark._

import scala.collection.mutable.ArrayBuffer

object ScalaTest {


  def main(args: Array[String]) {

//    val test = "11ABC회)생중"
//
//    val results = test.split("[0-9]+|[a-zA-Z]+|[^가-힣ㄱ-ㅎㅏ-ㅣ]")
//
//
//    println(results.mkString(", "))
//
//
//    val results2 = "ABC마트".split("[^가-힣ㄱ-ㅎㅏ-ㅣ]|[0-9]+")
//
//
//    println(results2.mkString(", "))
//
//
//    val results3 = "ㄷㅣ나라".split("[^가-힣ㄱ-ㅎㅏ-ㅣ]|[0-9]+")
//
//    println(results3.mkString(", "))



//    val t = "12월2일부터"
//    val m = Array(("12", 0), ("월", 1), ("2", 0), ("일", 2), ("부터", 3))

//    val t = "중·고등학교에서"
//    val m = Array(("중·고등학교", 217237), ("에서", 158689))

//    val t = "미·소만이"
//    val m = Array(("미소", 86089), ("만", 73975), ("이", 183508))

//    val t = "200만~300만원에"
//    val m = Array(("200", 0), ("만", 73977), ("~", 0), ("300", 0), ("원", 174730), ("에", 158321))

//    val t = "초/중교에"
//    val m = Array(("초중교", 232187), ("에", 158321))

//    val t = "10.26이"
//    val m = Array(("10.26", 18), ("이", 183510))

    val t = "공약:<1>시장"
    val m = Array(("공약", 19321), (":", 0), ("<", 0), ("1", 0), (">", 0), ("시장", 140895))

    var wordSeqs = ArrayBuffer[Int]()

    val words = ArrayBuffer[(String, Array[Int])]()

    var sentence = t
    val last = m.length - 1

    m.indices.foreach(i => {

      val r = m(i)

      println("row : " + r)

      if(r._2 == 0){

        val s = getSurface(sentence, r._1)
        println(s)

        val surface = s._1

        sentence = s._2

        if(surface.length > 0){

          println(surface + " : " + wordSeqs.mkString(", "))
          val t = (surface, wordSeqs.toArray)
          words += t
          wordSeqs = ArrayBuffer[Int]()
        }

      }else{
        wordSeqs += r._2
      }

      if(i == last){
        if(wordSeqs.nonEmpty){
          val t = (sentence, wordSeqs.toArray)
          words += t
        }
      }

    })

    words.foreach(w => {

      println(w._1 + " : " + w._2.mkString(", "))
    })


//    val t_1 = getHead(t, "12")

//    val t_2 = getHead(t_1._2, "2")

//    val t_3 = getHead(t_2._2, ",")

//    val t_4 = getHead(t_3._2, "00")


//    println(t_1)
//    println(t_2)
//    println(t_3)
//    println(t_4)
//    println(t_4._2)

  }

  def getSurface(sentence: String, word: String) = {

    val idx = sentence.indexOf(word)

    val end = idx
    val surface = sentence.substring(0, end)
    val leftSentence = sentence.substring(end + word.length)

    (surface, leftSentence) // r : surface, l : 남은 문자열
  }

}
