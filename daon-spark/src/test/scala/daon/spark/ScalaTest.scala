package daon.spark

import java.io.{ByteArrayOutputStream, FileInputStream}

import com.google.protobuf.CodedInputStream
import daon.analysis.ko.proto.Model
import daon.analysis.ko.util.{CharTypeChecker, Utils}
import daon.spark.PreProcess.Morpheme
import org.apache.commons.io.IOUtils
import org.apache.spark.sql.SparkSession
import org.elasticsearch.spark._

import scala.collection.mutable.ArrayBuffer

object ScalaTest {


  def main(args: Array[String]) {

//    val surface = "아시죠?\""
//    val morphemes = Seq(
//      Morpheme(1, "알", "VV"),
//      Morpheme(2, "시", "EP"),
//      Morpheme(3, "죠", "EF"),
//      Morpheme(4, "?", "SF"),
//      Morpheme(5, "\"", "SP")
//    )

//    val surface = "박혜룡(박혜룡)씨의"
//    val morphemes = Seq(
//      Morpheme(1, "박혜룡", "NNP"),
//      Morpheme(2, "씨", "NNB"),
//      Morpheme(3, "의", "EF")
//    )

//    val surface = "그래라이.\""
//    val morphemes = Seq(
//      Morpheme(1, "그러", "VV"),
//      Morpheme(2, "어라이", "EF"),
//      Morpheme(2, "!", "SF"),
//      Morpheme(2, "\"", "SP")
//    )

//    val surface = "박혜룡(박혜룡)씨의"
//    val morphemes = Seq(
//      Morpheme(1, "박혜룡", "NNP"),
//      Morpheme(2, "씨", "NNB"),
//      Morpheme(3, "의", "EF")
//    )

//    val surface = "(A)도식(민족주의)과"
//    val morphemes = Seq(
//      Morpheme(1, "(", "SP"),
//      Morpheme(2, "A", "SL"),
//      Morpheme(3, ")", "SP"),
//      Morpheme(4, "도식", "NNG"),
//      Morpheme(5, "(", "SP"),
//      Morpheme(6, "민족주의", "NNG"),
//      Morpheme(7, ")", "SP"),
//      Morpheme(8, "과", "JC")
//    )

//    val surface = "어디서…….\"(아낙네)"
//    val morphemes = Seq(
//      Morpheme(1, "어디", "NP"),
//      Morpheme(2, "서", "JKB"),
//      Morpheme(3, ".", "SF"),
//      Morpheme(4, "\"", "SP"),
//      Morpheme(5, "(", "SP"),
//      Morpheme(6, "아낙", "NNG"),
//      Morpheme(7, "네", "XSN"),
//      Morpheme(8, ")", "SP")
//    )

//    val surface = "어디서…….\"(아낙네)"
//    val morphemes = Seq(
//      Morpheme(1, "어디", "NP"),
//      Morpheme(2, "서", "JKB"),
//      Morpheme(3, ".", "SF"),
//      Morpheme(4, "\"", "SP"),
//      Morpheme(5, "(", "SP"),
//      Morpheme(6, "아낙", "NNG"),
//      Morpheme(7, "네", "XSN"),
//      Morpheme(8, ")", "SP")
//    )

//    val surface = "불러내가잖어."
//    val morphemes = Seq(
//      Morpheme(1, "불러내", "VV"),
//      Morpheme(2, "어", "EC"),
//      Morpheme(3, "가", "VX"),
//      Morpheme(4, "잖어", "EF"),
//      Morpheme(5, ".", "SP")
//    )

//    val surface = "박혜룡(박혜룡)씨의"
//    val morphemes = Seq(
//      Morpheme(1, "박혜룡", "NNP"),
//      Morpheme(2, "씨", "NNB"),
//      Morpheme(3, "의", "EF")
//    )

//    val surface = "어떡해!\"하며"
//    val morphemes = Seq(
//      Morpheme(1, "어떡하", "VV"),
//      Morpheme(2, "어", "EF"),
//      Morpheme(3, "!", "SF"),
//      Morpheme(4, "\"", "SP"),
//      Morpheme(5, "하", "VV"),
//      Morpheme(6, "며", "EC")
//    )

//    val surface = "주의(attention)란"
//    val morphemes = Seq(
//      Morpheme(1, "주의", "NNG"),
//      Morpheme(2, "(", "SP"),
//      Morpheme(3, "attention", "SL"),
//      Morpheme(4, ")", "SP"),
//      Morpheme(5, "이", "VCP"),
//      Morpheme(6, "란", "ETM")
//    )

//    val surface = "질렸다."
//    val morphemes = Seq(
//      Morpheme(1, "질리", "VV"),
//      Morpheme(2, "었", "EP"),
//      Morpheme(3, "다", "EF"),
//      Morpheme(5, ".", "SP")
//    )

//    val surface = "논고(론고)했다."
//    val morphemes = Seq(
//      Morpheme(1, "논고", "NNG"),
//      Morpheme(2, "하", "XSV"),
//      Morpheme(3, "었", "EP"),
//      Morpheme(4, "다", "EF"),
//      Morpheme(5, ".", "SP")
//    )

//    val surface = "성(성)노예전범"
//    val morphemes = Seq(
//      Morpheme(1, "성노예전범", "NNG")
//    )

    val surface = "나지이"
    val morphemes = Seq(
      Morpheme(1, "나", "NNG"),
      Morpheme(2, "이", "VCP"),
      Morpheme(3, "지이", "EC")
    )

    println("33333333333333333333333")

    MakeWordsFST.parsePartialWords3(surface, morphemes).foreach(println)


    println("22222222222222222222222")

    MakeWordsFST.parsePartialWords2(morphemes, surface).foreach(println)

//    val charType = CharTypeChecker.charType('林')
//    println(charType)

  }

}
