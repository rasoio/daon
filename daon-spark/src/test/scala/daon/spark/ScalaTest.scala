package daon.spark

import java.io.{ByteArrayOutputStream, FileInputStream}

import com.google.protobuf.CodedInputStream
import daon.core.proto.Model
import daon.core.util.{CharTypeChecker, Utils}
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

//    val surface = "나지이"
//    val morphemes = Seq(
//      Morpheme(1, "나", "NNG"),
//      Morpheme(2, "이", "VCP"),
//      Morpheme(3, "지이", "EC")
//    )
//
//    val word1 = "나"
//    val word2 = "이"
//
//    val word1 = "미끄러지"
//    val word2 = "어"

//
//    val surface = "아시죠?\""
//    val morphemes = Seq(
//      Morpheme(1, "알", "VV"),
//      Morpheme(2, "시", "EP"),
//      Morpheme(3, "죠", "EF"),
//      Morpheme(4, "?", "SF"),
//      Morpheme(5, "\"", "SP")
//    )
////
//    val word1 = "알"
//    val word2 = "시"


//    val surface = "흘러들어가는"
//    val morphemes = Seq(
//      Morpheme(1, "흐르", "VV"),
//      Morpheme(2, "어", "EC"),
//      Morpheme(3, "들어가", "VV"),
//      Morpheme(5, "는", "ETM")
//    )

//    val surface = "누군가가"
//    val morphemes = Seq(
//      Morpheme(1, "누구", "NP"),
//      Morpheme(2, "이", "VCP"),
//      Morpheme(3, "ㄴ가", "EC"),
//      Morpheme(4, "가", "JKS")
//    )

//    val surface = "미끄러져들어가"
//    val morphemes = Seq(
//      Morpheme(1, "미끄러지", "VV"),
//      Morpheme(2, "어", "EC"),
//      Morpheme(3, "들어가", "VV"),
//      Morpheme(4, "아", "EC")
//    )

//    val surface = "늦을까봐"
//    val morphemes = Seq(
//      Morpheme(1, "늦", "VV"),
//      Morpheme(2, "ㄹ까", "EC"),
//      Morpheme(3, "보", "VV"),
//     Morpheme(4, "아", "EC")
//    )

//        val surface = "가야지."
//        val morphemes = Seq(
//          Morpheme(1, "가", "VV"),
//          Morpheme(2, "아야지", "EF"),
//          Morpheme(3, ".", "SF")
//        )

//        val surface = "비조라고"
//        val morphemes = Seq(
//          Morpheme(1, "비조", "NNG"),
//          Morpheme(2, "이", "VCP"),
//          Morpheme(3, "라고", "EC")
//        )

//        val surface = "캐가"
//        val morphemes = Seq(
//          Morpheme(1, "캐", "VV"),
//          Morpheme(2, "어", "EC"),
//          Morpheme(3, "가", "VX"),
//          Morpheme(4, "아", "EC")
//        )
//

//    이때였습니다. :
//      이때  (NNG/일반 명사)
//    이  (VCP/긍정 지정사)
//    었  (EP/선어말 어미)
//    습니다  (EF/종결 어미)
//    .  (SF/마침표물음표,느낌표)

//    val surface = "누구에게선가"
//    val morphemes = Seq(
//      Morpheme(1, "누구", "NP"),
//      Morpheme(2, "에게서", "JKB"),
//      Morpheme(3, "이", "VCP"),
//      Morpheme(4, "ㄴ가", "EC")
//    )

//    val surface = "어떻겐가"
//    val morphemes = Seq(
//      Morpheme(1, "어떻게", "MAG"),
//      Morpheme(2, "이", "VCP"),
//      Morpheme(3, "ㄴ가", "EC")
//    )


    val surface = "이때였습니다"
    val morphemes = Seq(
      Morpheme(1, "이때", "NNG"),
      Morpheme(2, "이", "VCP"),
      Morpheme(3, "었", "EP"),
      Morpheme(4, "습니다", "EF")
    )

//    println("11111111111111111")
//    MakeWordsFST.parsePartialWords1(surface, morphemes).foreach(println)
//    println("22222222222222222")
//    MakeWordsFST.parsePartialWords2(surface, morphemes).foreach(println)
    println("33333333333333333")
    MakeWordsFST.parsePartialWords(surface, morphemes).foreach(println)

//    morphemes.slice(0, 0+2).foreach(println)

//    val beginIndex = 7
//    println(surface.substring(beginIndex))

//    val source = MakeWordsFST.removeEndWith(surface, morphemes, 0)
//    println(source)
//    MakeWordsFST.parsePartialWords(morphemes, surface).foreach(println)

//    val charType = CharTypeChecker.charType('林')
//    println(charType)

  }


}
