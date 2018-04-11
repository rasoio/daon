package daon.spark

import java.util

import daon.core.Daon
import daon.core.data.Eojeol
import org.apache.spark.sql._

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
  * 정확률 측정
  * 학습에 사용하지 않은 test용 sentences 대상
  */
object EvaluateModel {

  val daonAnalyzer = new Daon()
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
      .config("es.nodes.wan.only", "true") //only connects through the declared es.nodes
      .getOrCreate()

    evaluate(spark)

  }

  private def evaluate(spark: SparkSession) = {

    val df = spark.read.format("es").load(SENTENCES_INDEX_TYPE)
      .limit(10000) // 1만건 대상

    val evaluateSet = df

    val totalMorphCnt = spark.sparkContext.longAccumulator("totalMorphCnt")
    val totalMorphPredCnt = spark.sparkContext.longAccumulator("totalMorphPredCnt")
    val totalMorphCorrectCnt = spark.sparkContext.longAccumulator("totalMorphCorrectCnt")

    val totalEojeolCnt = spark.sparkContext.longAccumulator("totalEojeolCnt")
    val totalEojeolCorrectCnt = spark.sparkContext.longAccumulator("totalEojeolCorrectCnt")
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
//        val analyzeEojeol = analyze(surface).get(0)

        val analyzeMorphemes = analyzeEojeol.getMorphemes

        val analyzeWords = ArrayBuffer[Keyword]()

        for( keyword <- analyzeMorphemes.asScala ){
          analyzeWords += Keyword(keyword.getWord, keyword.getTag)
        }

        val correctWords = ArrayBuffer[Keyword]()

        morphemes.indices.foreach(m=>{
          val morpheme = morphemes(m)
          val seq = morpheme.getAs[Long]("seq")
          val word = morpheme.getAs[String]("word")
          val tag = morpheme.getAs[String]("tag")

          correctWords += Keyword(word, tag)
        })

        val (correctCnt, errorCnt) = check(correctWords, analyzeWords)

        val totalCnt = correctWords.size

        totalMorphCnt.add(totalCnt)
        totalMorphPredCnt.add(analyzeWords.size)
        totalMorphCorrectCnt.add(correctCnt)

        if(errorCnt > 0){
          val correctKeywords = correctWords.map(k=>k.word + "/" + k.tag).mkString("+")
          val analyzedKeywords = analyzeWords.map(k=>k.word + "/" + k.tag).mkString("+")

          // 에러 결과 별도 리포팅 필요
          println(s"$errorCnt : $surface => $correctKeywords || $analyzedKeywords << $sentence")

          totalEojeolErrorCnt.add(1)
        }else{
          totalEojeolCorrectCnt.add(1)
        }

      })
    })

    val totalEojeolCorrect = totalEojeolCnt.value - totalEojeolErrorCnt.value
    val totalMorphErrorCnt = totalMorphCnt.value - totalMorphCorrectCnt.value

    val eojeolAccuracyRatio = totalEojeolCorrect.toFloat / totalEojeolCnt.value.toFloat * 100
    val morphAccuracyRatio = totalMorphCorrectCnt.value.toFloat / totalMorphCnt.value.toFloat * 100

    println(s"eojeol accuracyRatio : $eojeolAccuracyRatio, error : ${totalEojeolErrorCnt.value}, total : ${totalEojeolCnt.value}")
    println(s"morph accuracyRatio : $morphAccuracyRatio, error : $totalMorphErrorCnt, total : ${totalMorphCnt.value}")

    val p = totalMorphCorrectCnt.value / totalMorphPredCnt.value.toFloat
    val r = totalMorphCorrectCnt.value / totalMorphCnt.value.toFloat

    val f1 = 2 * p * r / (p + r)

    println(s"precision : ${p * 100}")
    println(s"recall : ${r * 100}")
    println(s"f1 score : ${f1 * 100}")

  }

  private def analyze(sentence: String): util.List[Eojeol] = {
    val result = new util.ArrayList[Eojeol]()

    //에러난 경우..
    try{
      return daonAnalyzer.analyze(sentence)
    }catch {
      case e: NullPointerException => println(s"error => ${e.getMessage}, sentence => $sentence")
    }

    result
  }

  private def check(correct: ArrayBuffer[Keyword], analyzed: ArrayBuffer[Keyword]) = {
    var correctCnt = 0
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

      if(isExist){
        correctCnt += 1
      }else{
        errorCnt += 1
      }

    })

    (correctCnt, errorCnt)
  }

}
