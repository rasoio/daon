package daon.spark

import java.util
import java.util.{ArrayList, List}

import ch.qos.logback.classic.{Level, Logger}
import daon.analysis.ko.DaonAnalyzer
import daon.analysis.ko.model.{EojeolInfo, ModelInfo}
import daon.analysis.ko.reader.ModelReader
import org.apache.commons.lang3.time.StopWatch
import org.apache.spark.sql._
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.ArrayBuffer

/**
  * 재현율 측정용
  * 특수문자는 측정 제외 필요
  * (학습 데이터에서 특수문자 오매칭이 많음)
  */
object EvaluateModel {

  val model: ModelInfo = ModelReader.create.load
  val daonAnalyzer = new DaonAnalyzer(model)
  var ratioArr: ArrayBuffer[Float] = ArrayBuffer[Float]()

  case class Keyword(word:String, tag:String)

//  val SENTENCES_INDEX_TYPE = "sejong_train_sentences_v3/sentence"
  val SENTENCES_INDEX_TYPE = "test_sentences/sentence"

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
//      .master("spark://daon.spark:7077")
      .config("es.nodes", "localhost")
      .config("es.port", "9200")
      .getOrCreate()

    evaluate(spark)

  }

  private def evaluate(spark: SparkSession) = {

    val df = spark.read.format("es").load(SENTENCES_INDEX_TYPE)
//      .limit(10000) // 1만건 대상

    val evaluateSet = df

    val totalMorphCnt = spark.sparkContext.longAccumulator("totalMorphCnt")
    val totalMorphErrorCnt = spark.sparkContext.longAccumulator("totalMorphErrorCnt")

    val totalEojeolCnt = spark.sparkContext.longAccumulator("totalEojeolCnt")
    val totalEojeolErrorCnt = spark.sparkContext.longAccumulator("totalEojeolErrorCnt")

    evaluateSet.foreach(row =>{
      val sentence = row.getAs[String]("sentence")

      val results = analyze(sentence)

      val eojeols = row.getAs[Seq[Row]]("eojeols")

      totalEojeolCnt.add(eojeols.size)

      eojeols.indices.foreach(e=> {
        val eojeol = eojeols(e)
        val surface = eojeol.getAs[String]("surface")
        val morphemes = eojeol.getAs[Seq[Row]]("morphemes")

        val analyzeEojeol = results.get(e)
        val nodes = analyzeEojeol.getNodes

        val analyzeWords = ArrayBuffer[Keyword]()

        for ( node <- nodes ) {
          for( keyword <- node.getKeywords ){
            analyzeWords += Keyword(keyword.getWord, keyword.getTag.name)
          }
        }

        val correctWords = ArrayBuffer[Keyword]()

        morphemes.indices.foreach(m=>{
          val morpheme = morphemes(m)
          val seq = morpheme.getAs[Long]("seq")
          val word = morpheme.getAs[String]("word")
          val tag = morpheme.getAs[String]("tag")

          correctWords += Keyword(word, tag)
        })

        val errorCnt = check(correctWords, analyzeWords)

        val totalCnt = correctWords.size

        totalMorphCnt.add(totalCnt)
        totalMorphErrorCnt.add(errorCnt)

        if(errorCnt > 0){
          val correctKeywords = correctWords.map(k=>k.word + "/" + k.tag).mkString("+")
          val analyzedKeywords = analyzeWords.map(k=>k.word + "/" + k.tag).mkString("+")

          // 에러 결과 별도 리포팅 필요
          println(s"$errorCnt : $surface => $correctKeywords || $analyzedKeywords << $sentence")

          totalEojeolErrorCnt.add(1)
        }

      })
    })

    val eojeolAccuracyRatio = (totalEojeolCnt.value - totalEojeolErrorCnt.value).toFloat / totalEojeolCnt.value.toFloat * 100
    val morphAccuracyRatio = (totalMorphCnt.value - totalMorphErrorCnt.value).toFloat / totalMorphCnt.value.toFloat * 100

    println("eojeol accuracyRatio : " + eojeolAccuracyRatio + ", error : " + totalEojeolErrorCnt.value + ", total : " + totalEojeolCnt.value)
    println("morph accuracyRatio : " + morphAccuracyRatio + ", error : " + totalMorphErrorCnt.value + ", total : " + totalMorphCnt.value)
  }

  private def analyze(sentence: String): util.List[EojeolInfo] = {
    val result = new util.ArrayList[EojeolInfo]()

    //에러난 경우..
    try{
      result.addAll(daonAnalyzer.analyzeText(sentence))
    }catch {
      case e: NullPointerException => println(s"error => ${e.getMessage}, sentence => ${sentence}")
    }

    result
  }

  private def check(correct: ArrayBuffer[Keyword], analyzed: ArrayBuffer[Keyword]) = {
    var errorCnt = 0

    correct.indices.foreach(i=>{
      val a = correct(i)
      var b = Keyword("","")

      //존재하는 경우 정답으로, 위치가 틀어지는 경우 전체가 에러로 처리됨을 방지
      var isExist = false
      analyzed.foreach(m => {
        b = m
        if(a.word == b.word && a.tag == b.tag){
          isExist = true
        }
      })

      if(!isExist){
        errorCnt += 1
      }

    })

    errorCnt
  }


  private def checkBefore(correct: ArrayBuffer[Keyword], analyzed: ArrayBuffer[Keyword]) = {
    var errorCnt = 0

    correct.indices.foreach(i=>{
      val a = correct(i)
      var b = Keyword("","")

      if(i < analyzed.size){
        b = analyzed(i)
      }

      if(a.word != b.word || a.tag != b.tag){
        errorCnt += 1
      }

    })

    errorCnt
  }
}
