package daon.spark

import daon.spark.MakeWordsFST.PartialWordsTemp
import daon.spark.PreProcess.Morpheme
import org.junit.{Before, Test}
import org.scalatest.junit.JUnitSuite
import org.junit.Assert._

import scala.collection.mutable.ArrayBuffer

class TestMakeWordsFST extends JUnitSuite{

  case class TestSet(morphemes: Seq[Morpheme], surface: String, expected: ArrayBuffer[PartialWordsTemp])

  val testSets: ArrayBuffer[TestSet] = ArrayBuffer[TestSet]()

  @Before def initialize(): Unit = {

    //오매핑 케이스
    testSets += TestSet(Seq(
      Morpheme(1, "어떻", "VA"),
      Morpheme(2, "어서", "EC"),
      Morpheme(3, "…", "SP"),
      Morpheme(4, "…", "SP"),
      Morpheme(5, "\"", "SP")
    ),
      "어때서...\"",
      ArrayBuffer[PartialWordsTemp]()
    )

    testSets += TestSet(Seq(
      Morpheme(1, "그러", "VV"),
      Morpheme(2, "어라이", "EF"),
      Morpheme(2, "!", "SF"),
      Morpheme(2, "\"", "SP")
    ),
      "그래라이.\"",
      ArrayBuffer[PartialWordsTemp]()
    )

    testSets += TestSet(Seq(
      Morpheme(1, "간지르", "VV"),
      Morpheme(2, "어", "EC")
    ),
      "간지러",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("간지러",ArrayBuffer(1, 2),"f")
      )
    )

    testSets += TestSet(Seq(
      Morpheme(1, "법인", "NNG"),
      Morpheme(2, "이", "XSV"),
      Morpheme(3, "ㄴ", "EP"),
      Morpheme(4, "'", "SF"),
      Morpheme(5, "삼양제지공업주식회사", "EP"),
      Morpheme(6, "'", "SF"),
      Morpheme(7, "의", "XSV")
    ),
      "법인인'삼양제지공업주식회사'의",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("법인",ArrayBuffer(1),"f"),
        PartialWordsTemp("법인인",ArrayBuffer(1, 2, 3),"f"),
        PartialWordsTemp("인",ArrayBuffer(2, 3),"b"),
        PartialWordsTemp("삼양제지공업주식회사",ArrayBuffer(5),"f"),
        PartialWordsTemp("의",ArrayBuffer(7),"f")
      )
    )

//    testSets += TestSet(Seq(
//      Morpheme(1, "논고", "NNG"),
//      Morpheme(2, "하", "XSV"),
//      Morpheme(3, "었", "EP"),
//      Morpheme(4, "다", "EF"),
//      Morpheme(5, ".", "SF")
//    ),
//      "논고(론고)했다.",
//      ArrayBuffer[PartialWordsTemp](
//        PartialWordsTemp(논고,ArrayBuffer(1),f)
//        PartialWordsTemp(논고(론고)했,ArrayBuffer(1, 2, 3),f)
//        PartialWordsTemp(논고(론고)했다,ArrayBuffer(1, 2, 3, 4),f)
//        PartialWordsTemp((론고)했다,ArrayBuffer(2, 3, 4),b)
//        PartialWordsTemp(다,ArrayBuffer(4),b)
//      )
//    )

    testSets += TestSet(Seq(
      Morpheme(1, "30", "SN"),
      Morpheme(2, "만", "NR"),
      Morpheme(3, "50", "SN"),
      Morpheme(4, "만", "NR"),
      Morpheme(5, "원", "NNB"),
      Morpheme(6, "대", "XSN"),
      Morpheme(7, "의", "JKG")
    ),
      "30만～50만원대의",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("만",ArrayBuffer(2),"f"),
        PartialWordsTemp("만",ArrayBuffer(4),"f"),
        PartialWordsTemp("만원",ArrayBuffer(4, 5),"f"),
        PartialWordsTemp("만원대",ArrayBuffer(4, 5, 6),"f"),
        PartialWordsTemp("만원대의",ArrayBuffer(4, 5, 6, 7),"f"),
        PartialWordsTemp("원대의",ArrayBuffer(5, 6, 7),"b"),
        PartialWordsTemp("대의",ArrayBuffer(6, 7),"b"),
        PartialWordsTemp("의",ArrayBuffer(7),"b")
      )
    )

    testSets += TestSet(Seq(
      Morpheme(1, "수", "NNG"),
      Morpheme(2, "는", "JX"),
      Morpheme(3, "…", "SP"),
      Morpheme(4, "에", "IC"),
      Morpheme(5, "…", "SP"),
      Morpheme(6, "모르", "VV"),
      Morpheme(7, "겠", "EP"),
      Morpheme(8, "습니다만", "EC")
    ),
      "수는...에...모르겠습니다만",
      ArrayBuffer[PartialWordsTemp]()
    )

    testSets += TestSet(Seq(
      Morpheme(1, "박혜룡", "NNP"),
      Morpheme(2, "씨", "NNB"),
      Morpheme(3, "의", "EF")
    ),
      "박혜룡(박혜룡)씨의",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("박혜룡",ArrayBuffer(1),"f"),
        PartialWordsTemp("박혜룡씨",ArrayBuffer(1, 2),"f"),
        PartialWordsTemp("박혜룡씨의",ArrayBuffer(1, 2, 3),"f"),
        PartialWordsTemp("씨의",ArrayBuffer(2, 3),"b"),
        PartialWordsTemp("의",ArrayBuffer(3),"b")
      )
    )

    testSets += TestSet(Seq(
      Morpheme(1, "알", "VV"),
      Morpheme(2, "시", "EP"),
      Morpheme(3, "죠", "EF"),
      Morpheme(4, "?", "SF"),
      Morpheme(5, "\"", "SP")
    ),
      "아시죠?\"",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("아",ArrayBuffer(1),"f"),
        PartialWordsTemp("아시",ArrayBuffer(1, 2),"f"),
        PartialWordsTemp("아시죠",ArrayBuffer(1, 2, 3),"f"),
        PartialWordsTemp("시죠",ArrayBuffer(2, 3),"b"),
        PartialWordsTemp("죠",ArrayBuffer(3),"b")
      )
    )

    testSets += TestSet(Seq(
      Morpheme(1, "누나", "NNG"),
      Morpheme(2, "아", "JKV"),
      Morpheme(3, "!", "SP"),
      Morpheme(4, "\"", "SP")
    ),
      "\"누나아!\"",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("누나",ArrayBuffer(1),"f"),
        PartialWordsTemp("누나아",ArrayBuffer(1, 2),"f"),
        PartialWordsTemp("아",ArrayBuffer(2),"b")
      )
    )

    testSets += TestSet(Seq(
      Morpheme(1, "어디", "NP"),
      Morpheme(2, "서", "JKB"),
      Morpheme(3, ".", "SF"),
      Morpheme(4, "\"", "SP"),
      Morpheme(5, "(", "SP"),
      Morpheme(6, "아낙", "NNG"),
      Morpheme(7, "네", "XSN"),
      Morpheme(8, ")", "SP")
    ),
      "어디서…….\"(아낙네)",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("어디",ArrayBuffer(1),"f"),
        PartialWordsTemp("어디서",ArrayBuffer(1, 2),"f"),
        PartialWordsTemp("서",ArrayBuffer(2),"b"),
        PartialWordsTemp("아낙",ArrayBuffer(6),"f"),
        PartialWordsTemp("아낙네",ArrayBuffer(6, 7),"f"),
        PartialWordsTemp("네",ArrayBuffer(7),"b")
      )
    )

    testSets += TestSet(Seq(
      Morpheme(1, "어떡하", "VV"),
      Morpheme(2, "어", "EF"),
      Morpheme(3, "!", "SF"),
      Morpheme(4, "\"", "SP"),
      Morpheme(5, "하", "VV"),
      Morpheme(6, "며", "EC")
    ),
      "어떡해!\"하며",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("어떡해",ArrayBuffer(1, 2),"f"),
        PartialWordsTemp("하",ArrayBuffer(5),"f"),
        PartialWordsTemp("하며",ArrayBuffer(5, 6),"f"),
        PartialWordsTemp("며",ArrayBuffer(6),"b")
      )
    )

    testSets += TestSet(Seq(
      Morpheme(1, "'", "SP"),
      Morpheme(2, "위대", "NNG"),
      Morpheme(3, "하", "XSA"),
      Morpheme(4, "ㅁ", "ETN"),
      Morpheme(5, "(", "SP"),
      Morpheme(6, "Grossheit", "SL"),
      Morpheme(7, ")", "SP"),
      Morpheme(8, "'", "SP"),
      Morpheme(9, "에", "JKB")
    ),
      "'위대함(Grossheit)'에",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("위대",ArrayBuffer(2),"f"),
        PartialWordsTemp("위대함",ArrayBuffer(2, 3, 4),"f"),
        PartialWordsTemp("함",ArrayBuffer(3, 4),"b"),
        PartialWordsTemp("에",ArrayBuffer(9),"f")
      )
    )

    testSets += TestSet(Seq(
      Morpheme(1, "'", "SP"),
      Morpheme(2, "파워주택자금대출", "NNG"),
      Morpheme(3, "'", "SP"),
      Morpheme(4, "시행", "NNG"),
      Morpheme(5, "으로", "JKB")
    ),
      "'파워주택자금대출'시행으로",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("파워주택자금대출",ArrayBuffer(2),"f"),
        PartialWordsTemp("시행",ArrayBuffer(4),"f"),
        PartialWordsTemp("시행으로",ArrayBuffer(4, 5),"f"),
        PartialWordsTemp("으로",ArrayBuffer(5),"b")
      )
    )

    testSets += TestSet(Seq(
        Morpheme(1, "다", "EC")
      ),
      "다.",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("다", ArrayBuffer(1), "f")
      )
    )

    testSets += TestSet(Seq(
        Morpheme(1, "진행", "NNG"),
        Morpheme(2, "되", "VV"),
        Morpheme(3, "ㄹ", "EF")
      ),
      "진행될",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("진행", ArrayBuffer(1), "f"),
        PartialWordsTemp("진행될", ArrayBuffer(1,2,3), "f"),
        PartialWordsTemp("될", ArrayBuffer(2,3), "b")
      )
    )

    testSets += TestSet(Seq(
        Morpheme(1, "꺼내", "VV"),
        Morpheme(2, "어", "EC")
      ),
      "꺼내",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("꺼내", ArrayBuffer(1,2), "f")
      )
    )

    testSets += TestSet(Seq(
        Morpheme(1, "진한영", "NNP"),
        Morpheme(2, "(", "SP"),
        Morpheme(3, "양천여고", "NNP")
      ),
      "진한영(양천여고",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("진한영", ArrayBuffer(1), "f"),
        PartialWordsTemp("양천여고", ArrayBuffer(3), "f")
      )
    )

    testSets += TestSet(Seq(
        Morpheme(1, "주의", "NNG"),
        Morpheme(2, "(", "SP"),
        Morpheme(3, "attention", "SL"),
        Morpheme(4, ")", "SP"),
        Morpheme(5, "이", "VCP"),
        Morpheme(6, "란", "ETM")
      ),
      "주의(attention)란",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("주의", ArrayBuffer(1), "f"),
        PartialWordsTemp("란", ArrayBuffer(5,6), "f")
      )
    )

    testSets += TestSet(Seq(
        Morpheme(1, "(", "SP"),
        Morpheme(2, "A", "SL"),
        Morpheme(3, ")", "SP"),
        Morpheme(4, "도식", "NNG"),
        Morpheme(5, "(", "SP"),
        Morpheme(6, "민족주의", "NNG"),
        Morpheme(7, ")", "SP"),
        Morpheme(8, "과", "JC")
      ),
      "(A)도식(민족주의)과",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("도식", ArrayBuffer(4), "f"),
        PartialWordsTemp("민족주의", ArrayBuffer(6), "f"),
        PartialWordsTemp("과", ArrayBuffer(8), "f")
      )
    )

    testSets += TestSet(Seq(
        Morpheme(1, "5", "SP"),
        Morpheme(2, ",", "SL"),
        Morpheme(3, "000", "SP"),
        Morpheme(4, "엔", "NNG"),
        Morpheme(5, ",", "SP")
      ),
      "5,000엔,",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("엔", ArrayBuffer(4), "f")
      )
    )

    testSets += TestSet(Seq(
        Morpheme(1, "불러내", "VV"),
        Morpheme(2, "가", "VX"),
        Morpheme(3, "잖어", "EF")
      ),
      "불러내가잖어",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("불러내", ArrayBuffer(1), "f"),
        PartialWordsTemp("불러내가", ArrayBuffer(1,2), "f"),
        PartialWordsTemp("불러내가잖어", ArrayBuffer(1,2,3), "f"),
        PartialWordsTemp("가잖어", ArrayBuffer(2,3), "b"),
        PartialWordsTemp("잖어", ArrayBuffer(3), "b")
      )
    )

    testSets += TestSet(Seq(
        Morpheme(1, "불러내", "VV"),
        Morpheme(2, "어", "EC"),
        Morpheme(3, "가", "VX"),
        Morpheme(4, "잖어", "EF"),
        Morpheme(5, ".", "SP")
      ),
      "불러내가잖어.",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("불러내", ArrayBuffer(1,2), "f"),
        PartialWordsTemp("불러내가", ArrayBuffer(1,2,3), "f"),
        PartialWordsTemp("불러내가잖어", ArrayBuffer(1,2,3,4), "f"),
        PartialWordsTemp("가잖어", ArrayBuffer(3,4), "b"),
        PartialWordsTemp("잖어", ArrayBuffer(4), "b")
      )
    )

    testSets += TestSet( Seq(
        Morpheme(1, "'", "SP"),
        Morpheme(2, "朝鮮國太白山端宗大王之碑", "SH"),
        Morpheme(3, "'", "SP"),
        Morpheme(4, "이", "VCP"),
        Morpheme(5, "라", "EC")
      ),
      "'朝鮮國太白山端宗大王之碑'라",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("라", ArrayBuffer(4,5), "f")
      )
    )

    testSets += TestSet( Seq(
        Morpheme(1, "10월민중항쟁", "NNP"),
        Morpheme(2, "이", "VCP"),
        Morpheme(3, "었", "EP"),
        Morpheme(4, "다", "EF")
      ),
      "10월민중항쟁이었다",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("10월민중항쟁",ArrayBuffer(1),"f"),
        PartialWordsTemp("10월민중항쟁이",ArrayBuffer(1, 2),"f"),
        PartialWordsTemp("10월민중항쟁이었",ArrayBuffer(1, 2, 3),"f"),
        PartialWordsTemp("10월민중항쟁이었다",ArrayBuffer(1, 2, 3, 4),"f"),
        PartialWordsTemp("이었다",ArrayBuffer(2, 3, 4),"b"),
        PartialWordsTemp("었다",ArrayBuffer(3, 4),"b"),
        PartialWordsTemp("다",ArrayBuffer(4),"b")
      )
    )

    testSets += TestSet(Seq(
        Morpheme(1, "돌아가", "NNG"),
        Morpheme(2, "아", "VV"),
        Morpheme(3, "달", "EF"),
        Morpheme(4, "라고", "EF")
      ),
      "돌아가달라고",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("돌아가", ArrayBuffer(1,2), "f"),
        PartialWordsTemp("돌아가달", ArrayBuffer(1,2,3), "f"),
        PartialWordsTemp("돌아가달라고", ArrayBuffer(1,2,3,4), "f"),
        PartialWordsTemp("달라고", ArrayBuffer(3,4), "b"),
        PartialWordsTemp("라고", ArrayBuffer(4), "b")
      )
    )

    testSets += TestSet( Seq(
      Morpheme(1, "질리", "VV"),
      Morpheme(2, "었", "EP"),
      Morpheme(3, "다", "EF"),
      Morpheme(5, ".", "SP")
    ),
      "질렸다.",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("질렸", ArrayBuffer(1,2), "f"),
        PartialWordsTemp("질렸다", ArrayBuffer(1,2,3), "f"),
        PartialWordsTemp("다", ArrayBuffer(3), "b")
      )
    )

    testSets += TestSet( Seq(
      Morpheme(1, "나", "NNG"),
      Morpheme(2, "이", "VCP"),
      Morpheme(3, "지이", "EC")
    ),
      "나지이",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("나", ArrayBuffer(1,2), "f"),
        PartialWordsTemp("나지이", ArrayBuffer(1,2,3), "f"),
        PartialWordsTemp("지이", ArrayBuffer(3), "b")
      )
    )

    testSets += TestSet( Seq(
      Morpheme(1, "미끄러지", "VV"),
      Morpheme(2, "어", "EC"),
      Morpheme(3, "들어가", "VV"),
      Morpheme(4, "아", "EC")
    ),
      "미끄러져들어가",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("미끄러져", ArrayBuffer(1,2), "f"),
        PartialWordsTemp("미끄러져들어가", ArrayBuffer(1,2,3,4), "f"),
        PartialWordsTemp("들어가", ArrayBuffer(3,4), "b")
      )
    )

    testSets += TestSet( Seq(
      Morpheme(1, "흐르", "VV"),
      Morpheme(2, "어", "EC"),
      Morpheme(3, "들어가", "VV"),
      Morpheme(4, "는", "ETM")
    ),
      "흘러들어가는",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("흘러",ArrayBuffer(1, 2),"f"),
        PartialWordsTemp("흘러들어가",ArrayBuffer(1, 2, 3),"f"),
        PartialWordsTemp("흘러들어가는",ArrayBuffer(1, 2, 3, 4),"f"),
        PartialWordsTemp("들어가는",ArrayBuffer(3, 4),"b"),
        PartialWordsTemp("는",ArrayBuffer(4),"b")
      )
    )

    testSets += TestSet( Seq(
      Morpheme(1, "흐르", "VV"),
      Morpheme(2, "어", "EC"),
      Morpheme(3, "들어가", "VV"),
      Morpheme(4, "아", "EC")
    ),
      "흘러들어가",
      ArrayBuffer[PartialWordsTemp](
//        PartialWordsTemp("흘러",ArrayBuffer(1, 2),"f"),
        PartialWordsTemp("흘러들어가",ArrayBuffer(1, 2, 3, 4),"f")
//        PartialWordsTemp("들어가",ArrayBuffer(3, 4),"b")
      )
    )

    testSets += TestSet( Seq(
      Morpheme(1, "누구", "NP"),
      Morpheme(2, "이", "VCP"),
      Morpheme(3, "ㄴ가", "EC"),
      Morpheme(4, "가", "JKS")
    ),
      "누군가가",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("누군가",ArrayBuffer(1, 2, 3),"f"),
        PartialWordsTemp("누군가가",ArrayBuffer(1, 2, 3, 4),"f"),
        PartialWordsTemp("가",ArrayBuffer(4),"b")
      )
    )

    testSets += TestSet( Seq(
      Morpheme(1, "늦", "VV"),
      Morpheme(2, "ㄹ까", "EC"),
      Morpheme(3, "보", "VV"),
      Morpheme(4, "아", "EC")
    ),
      "늦을까봐",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("늦",ArrayBuffer(1),"f"),
        PartialWordsTemp("늦을까봐",ArrayBuffer(1, 2, 3, 4),"f"),
        PartialWordsTemp("을까봐",ArrayBuffer(2, 3, 4),"b")
      )
    )

    testSets += TestSet( Seq(
      Morpheme(1, "가", "VV"),
      Morpheme(2, "아야지", "EF"),
      Morpheme(3, ".", "SF")
    ),
      "가야지.",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("가",ArrayBuffer(1),"f"),
        PartialWordsTemp("가야지",ArrayBuffer(1, 2),"f"),
        PartialWordsTemp("야지",ArrayBuffer(2),"b")
      )
    )

    testSets += TestSet( Seq(
      Morpheme(1, "걸리", "VV"),
      Morpheme(2, "었", "EP"),
      Morpheme(3, "ㄹ지", "EC"),
      Morpheme(4, "도", "JX")
    ),
      "걸렸을지도",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("걸렸을지",ArrayBuffer(1, 2, 3),"f"),
        PartialWordsTemp("걸렸을지도",ArrayBuffer(1, 2, 3, 4),"f"),
        PartialWordsTemp("도",ArrayBuffer(4),"b")
      )
    )

    testSets += TestSet( Seq(
      Morpheme(1, "오래되", "VV"),
      Morpheme(2, "ㅁ", "ETN"),
      Morpheme(3, "을", "JKO")
    ),
      "오래됨을",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("오래됨",ArrayBuffer(1, 2),"f"),
        PartialWordsTemp("오래됨을",ArrayBuffer(1, 2, 3),"f"),
        PartialWordsTemp("을",ArrayBuffer(3),"b")
      )
    )

    testSets += TestSet( Seq(
      Morpheme(1, "죽", "VV"),
      Morpheme(2, "ㄹ까", "EC"),
      Morpheme(3, "보", "VX"),
      Morpheme(4, "아", "EC")
    ),
      "죽을까봐",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("죽",ArrayBuffer(1),"f"),
        PartialWordsTemp("죽을까봐",ArrayBuffer(1, 2, 3, 4),"f"),
        PartialWordsTemp("을까봐",ArrayBuffer(2, 3, 4),"b")
      )
    )

    testSets += TestSet( Seq(
      Morpheme(1, "보이", "VV"),
      Morpheme(2, "ㄹ락", "EC"),
      Morpheme(3, "말", "VV"),
      Morpheme(4, "ㄹ락", "EC"),
      Morpheme(5, "하", "VX"),
      Morpheme(6, "게", "EC")
    ),
      "보일락말락하게",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("보일락말락",ArrayBuffer(1, 2, 3, 4),"f"),
        PartialWordsTemp("보일락말락하",ArrayBuffer(1, 2, 3, 4, 5),"f"),
        PartialWordsTemp("보일락말락하게",ArrayBuffer(1, 2, 3, 4, 5, 6),"f"),
        PartialWordsTemp("하게",ArrayBuffer(5, 6),"b"),
        PartialWordsTemp("게",ArrayBuffer(6),"b")
      )
    )

    testSets += TestSet( Seq(
      Morpheme(1, "외롭", "VA"),
      Morpheme(2, "시", "EP"),
      Morpheme(3, "ㄴ가", "EC"),
      Morpheme(4, "보", "EX"),
      Morpheme(5, "아요", "EF")
    ),
      "외로우신가봐요",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("외로우신가봐요",ArrayBuffer(1, 2, 3, 4, 5),"f")
      )
    )


    testSets += TestSet( Seq(
      Morpheme(1, "이때", "NNG"),
      Morpheme(2, "이", "VCP"),
      Morpheme(3, "었", "EP"),
      Morpheme(4, "습니다", "EF")
    ),
      "이때였습니다",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("외로우신가봐요",ArrayBuffer(1, 2, 3, 4, 5),"f")
      )
    )




  }

  @Test def verifyPartialWords() {

//    println(testSets.size)

    testSets.foreach(t => {
      println(t.surface)

//      val results = MakeWordsFST.parsePartialWords2(t.morphemes, t.surface)
      val results = MakeWordsFST.parsePartialWords(t.surface, t.morphemes)

      println("=========================")
      results.foreach(
        r=>{
          val last = results.last
          val comma = if(last != r) "," else ""

          println(
            s"""PartialWordsTemp("${r.surface}",${r.wordSeqs},"${r.direction}")${comma}""".stripMargin
          )
        }
      )

//      assertEquals(t.expected.size, results.size)

      results.indices.foreach(i=>{

//        if(i >= t.expected.size){
//          println(t.surface)
//        }

//        val a = t.expected(i)
//        val b = results(i)

//        assertEquals(a, b)
      })
    })
  }

}
