package daon.dictionary.spark

import java.io.File
import java.util

import org.apache.commons.io.FileUtils
import org.apache.spark.sql._
import org.json4s.{CustomSerializer, DefaultFormats}
import org.json4s.JsonAST.JNull
import org.json4s.jackson.Serialization.write

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex.MatchData

/**
  * seq 할당 받은 word 결과를 가진 json 학습 데이터를 만들기 위한 job
  * 초기 한번 정도만 사용
  * 이후 버전은 관리기 사용할수 있도록 구성
  */
object SejongToJson {

  case class Sentence(sentence: String, eojeols: ArrayBuffer[Eojeol] = ArrayBuffer[Eojeol]())

  case class Eojeol(seq: Long, surface: String, offset: Long, morphemes: ArrayBuffer[Morpheme] = ArrayBuffer[Morpheme]())

  case class Morpheme(seq: Long, word: String, tag: String,
                      var prevOuter: Option[BaseMorpheme] = None, var nextOuter: Option[BaseMorpheme] = None,
                      var prevInner: Option[BaseMorpheme] = None, var nextInner: Option[BaseMorpheme] = None)


  case class BaseMorpheme(seq: Long, word: String, tag: String)

  var irrCnt:Int = 0

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
      .getOrCreate()


    val jsonFile = new File("/Users/mac/work/corpus/sejong_utagger.json")

    //initialize
    FileUtils.write(jsonFile, "", "UTF-8")



    //선 추출 된 사전 단어...
    val wordInfoDF = spark.read.json("/Users/mac/work/corpus/utagger_words")

    wordInfoDF.printSchema()

    val wordInfoMap = wordInfoDF.rdd.map(r => {

      val seq = r.getAs[Long]("seq")
      val word = r.getAs[String]("word")
      val tag = r.getAs[String]("tag")
      val desc = r.getAs[String]("desc")
      val num = r.getAs[String]("num")

      var key = word + "/" + tag
      val value = seq

      if(num != null){
        key = word + "__" + num + "/" + tag
      }

      (key, value)
    }).collect().toMap[String, Long]


    // 후 매칭을 위한 학습용 문장/분석 결과
    // Create an RDD
    val posRDD = spark.sparkContext.textFile("/Users/mac/work/corpus/sejong_utagger.pos")
    val txtRDD = spark.sparkContext.textFile("/Users/mac/work/corpus/sejong_utagger.txt")

    val posArr = posRDD.collect()
    val txtArr = txtRDD.collect()

    posArr.indices.foreach(i => {
      val pos = posArr(i).trim
      val txt = txtArr(i).trim

      val posWord = pos.split("\\s+")
      val txtWord = txt.split("\\s+")

      val sentence = Sentence(txt)

      val m = ("\\s".r findAllIn txt).matchData.toArray[MatchData]

      posWord.indices.foreach(j => {
        val subPos = posWord(j)
        val subTxt = txtWord(j)

        var offset = 0

        if(j > 0){
          offset = m(j-1).end
        }

        val eojeol = Eojeol(seq = j, surface = subTxt, offset = offset)

        val morphs = subPos.split("(?<=[A-Z]{2,3})[+](?![/]S)")

        morphs.indices.foreach(m => {
          val morph = morphs(m)

          val morphemeInfo = morph.split("[/](?=[A-Z]{2,3})")

          var word = morphemeInfo(0)
          val tag = morphemeInfo(1)

          //word seq 추출
          val key = word + "/" + tag

          val seq = wordInfoMap.getOrElse[Long](key, 0)

          if(seq == 0 && !List("SL","SH","SN").contains(tag)){
            //학습 데이터 확인 필요 케이스
//            println(word, tag, i+1)
          }else{
            val idx = word.indexOf("__")
            if(idx > -1){
              word = word.substring(0, idx)
            }
          }

          val morpheme = Morpheme(seq = seq, word = word, tag = tag)

          eojeol.morphemes += morpheme

        })

        sentence.eojeols += eojeol

      })

      //형태소 연결 설정
      setInOutMorpheme(sentence)

      implicit val formats = DefaultFormats
      val jsonString = write(sentence)

//      println(jsonString)
      FileUtils.write(jsonFile, jsonString + System.lineSeparator, "UTF-8", true)

      if(i%10000 == 0){
        println(i)
      }
    })


    println(irrCnt)
  }

  private def setInOutMorpheme(sentence: Sentence) {

    val eojeols = sentence.eojeols

    val eHead = eojeols.head
    val elast = eojeols.last

    eojeols.indices.foreach(i => {
      val eojeol = eojeols(i)
      val morphemes = eojeol.morphemes

      var prevOuter = None : Option[BaseMorpheme]
      var nextOuter = None : Option[BaseMorpheme]

      if(eojeol != eHead){
        prevOuter = Option(copy(eojeols(i-1).morphemes.last))
      }

      if(eojeol != elast){
        nextOuter = Option(copy(eojeols(i+1).morphemes.head))
      }

      //어절간 연결 설정
      val mHead = morphemes.head
      val mLast = morphemes.last

      mHead.prevOuter = prevOuter
      mLast.nextOuter = nextOuter

      morphemes.indices.foreach(m => {
        val morpheme = morphemes(m)

        var prevInner = None : Option[BaseMorpheme]
        var nextInner = None : Option[BaseMorpheme]

        if(morpheme != mHead){
          prevInner = Option(copy(morphemes(m-1)))
        }

        if(morpheme != mLast){
          nextInner = Option(copy(morphemes(m+1)))
        }

        morpheme.prevInner = prevInner
        morpheme.nextInner = nextInner

      })

    })

  }


  def copy(morpheme: Morpheme): BaseMorpheme = {
    val seq = morpheme.seq
    val word = morpheme.word
    val tag = morpheme.tag

    BaseMorpheme(seq, word, tag)
  }

}
