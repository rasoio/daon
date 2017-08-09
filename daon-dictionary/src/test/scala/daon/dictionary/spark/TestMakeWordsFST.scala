package daon.dictionary.spark

import daon.dictionary.spark.MakeWordsFST.PartialWordsTemp
import daon.dictionary.spark.PreProcess.Morpheme
import org.junit.{Before, Test}
import org.scalatest.junit.JUnitSuite
import org.junit.Assert._

import scala.collection.mutable.ArrayBuffer

class TestMakeWordsFST extends JUnitSuite{

  case class TestSet(morphemes: Seq[Morpheme], surface: String, expected: ArrayBuffer[PartialWordsTemp])

  val testSets: ArrayBuffer[TestSet] = ArrayBuffer[TestSet]()

  @Before def initialize(): Unit = {

    testSets += TestSet(Seq(
        Morpheme(1, "다", "EC")
      ),
      "다.",
      ArrayBuffer[PartialWordsTemp]()
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
        PartialWordsTemp("아시죠", ArrayBuffer(1,2,3), "f")
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
        Morpheme(1, "돌아가", "NNG"),
        Morpheme(2, "아", "VV"),
        Morpheme(3, "달", "EF"),
        Morpheme(4, "라고", "EF")
      ),
      "돌아가달라고",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("돌아가", ArrayBuffer(1), "f"),
        PartialWordsTemp("돌아가달라고", ArrayBuffer(1,2,3,4), "f"),
        PartialWordsTemp("달라고", ArrayBuffer(2,3,4), "b"),
        PartialWordsTemp("라고", ArrayBuffer(4), "b")
      )
    )

    testSets += TestSet(Seq(
        Morpheme(1, "꺼내", "VV"),
        Morpheme(2, "어", "EC")
      ),
      "꺼내",
      ArrayBuffer[PartialWordsTemp](
        PartialWordsTemp("꺼내", ArrayBuffer(1), "f"),
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
        PartialWordsTemp("불러내", ArrayBuffer(1), "f"),
        PartialWordsTemp("불러내가잖어", ArrayBuffer(1,2,3,4), "f"),
        PartialWordsTemp("가잖어", ArrayBuffer(2,3,4), "b"),
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
        PartialWordsTemp("이", ArrayBuffer(2), "f"),
        PartialWordsTemp("이었", ArrayBuffer(2,3), "f"),
        PartialWordsTemp("이었다", ArrayBuffer(2,3,4), "f"),
        PartialWordsTemp("었다", ArrayBuffer(3,4), "b"),
        PartialWordsTemp("다", ArrayBuffer(4), "b")
      )
    )

  }

  @Test def verifyPartialWords() {

    testSets.foreach(t => {
      val results = MakeWordsFST.parsePartialWords(t.morphemes, t.surface)

      results.indices.foreach(i=>{
        if(i >= t.expected.size){
          println(t.surface)
        }
        val a = t.expected(i)
        val b = results(i)

        assertEquals(a, b)
      })
    })
  }

}
