package daon.dictionary.spark

import daon.analysis.ko.DaonAnalyzer
import daon.analysis.ko.model.ModelInfo
import daon.analysis.ko.reader.ModelReader
import org.apache.commons.lang3.time.StopWatch
import org.apache.spark.sql._

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.ArrayBuffer

/**
  * 재현율 측정용
  * 특수문자는 측정 제외 필요
  * (학습 데이터에서 특수문자 오매칭이 많음)
  */
object EvaluateModel {

//  val model: ModelInfo = ModelReader.create.filePath("/Users/mac/work/corpus/model/model8.dat").load
  val model: ModelInfo = ModelReader.create.load
  val daonAnalyzer = new DaonAnalyzer(model)
  var ratioArr: ArrayBuffer[Float] = ArrayBuffer[Float]()

  case class Keyword(word:String, tag:String)

//  val SENTENCES_INDEX_TYPE = "train_sentences_v2/sentence"
  val SENTENCES_INDEX_TYPE = "test_sentences_v2/sentence"

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
      .config("es.nodes", "localhost")
      .config("es.port", "9200")
      .config("es.index.auto.create", "true")
      .getOrCreate()

    readEs(spark)

    //기분석 사전 재현율 측정 필요. 사전 구분 및 위치 구분 확인
    // 앞부분(어절 시작 부분)에 forwardFst 사전 데이터가 나오는지
    // 뒷부분(시작 형태소 이후 형태소)에 backwardFst 데이터가 나오는지

  }

  private def readEs(spark: SparkSession) = {

    //배열 필드 지정 필요
    val options = Map(
      "es.read.field.as.array.include" -> "word_seqs"
    )

    val df = spark.read.format("es").options(options).load(SENTENCES_INDEX_TYPE)
//      .limit(10000)

    val evaluateSet = df

//    df.printSchema()
//    df.createOrReplaceTempView("sentence")

    val watch = new StopWatch

    watch.start()

//    var totalEojeolCnt = 0

    val totalMorphCnt = spark.sparkContext.longAccumulator("totalMorphCnt")
    val totalMorphErrorCnt = spark.sparkContext.longAccumulator("totalMorphErrorCnt")

    val totalEojeolCnt = spark.sparkContext.longAccumulator("totalEojeolCnt")
    val totalEojeolErrorCnt = spark.sparkContext.longAccumulator("totalEojeolErrorCnt")
//    val ratioArr = spark.sparkContext.collectionAccumulator[Float]("ratioArr")

    evaluateSet.foreach(row =>{
      val sentence = row.getAs[String]("sentence")

      val results = analyze(sentence)

      val eojeols = row.getAs[Seq[Row]]("eojeols")

      totalEojeolCnt.add(eojeols.size)

      eojeols.indices.foreach(e=> {
        val eojeol = eojeols(e)
        val surface = eojeol.getAs[String]("surface")
        val morphemes = eojeol.getAs[Seq[Row]]("morphemes")

        val r = results.get(e)
        val r_surface = r.getEojeol
        val r_terms = r.getTerms

        val analyzeWords = ArrayBuffer[Keyword]()

        for ( term <- r_terms ) {
          for( keyword <- term.getKeywords ){
            analyzeWords += Keyword(keyword.getWord, keyword.getTag.name)
          }
        }

//        println(surface, r_surface)

        val correctWords = ArrayBuffer[Keyword]()

        morphemes.indices.foreach(m=>{
          val morpheme = morphemes(m)
          val seq = morpheme.getAs[Long]("seq")
          val w = morpheme.getAs[String]("word")
          val tag = morpheme.getAs[String]("tag")

          correctWords += Keyword(w, tag)
        })

//        println(wordSeqs, r_wordSeqs)
        val errorCnt = check(correctWords, analyzeWords)

        //정확률
        val totalCnt = correctWords.size
        val correctCnt = totalCnt - errorCnt

        totalMorphCnt.add(totalCnt)
        totalMorphErrorCnt.add(errorCnt)

        if(errorCnt > 0){
          // 에러 결과 별도 리포팅 필요
//          println(errorCnt, surface, getKeyword(wordSeqs, r_wordSeqs))

          val correctKeywords = correctWords.map(k=>k.word + "/" + k.tag).mkString("+")

          val analyzedKeywords = analyzeWords.map(k=>k.word + "/" + k.tag).mkString("+")

          println(s"$errorCnt : $surface => $correctKeywords || $analyzedKeywords << $sentence")

          totalEojeolErrorCnt.add(1)
        }

      })
    })

    watch.stop()

    val eojeolAccuracyRatio = 100 - ((totalEojeolErrorCnt.value.toFloat / totalEojeolCnt.value.toFloat) * 100)
    val morphAccuracyRatio = 100 - ((totalMorphErrorCnt.value.toFloat / totalMorphCnt.value.toFloat) * 100)

//    println("avgRatio : " + avgRatio + ", totalEojeolCnt : " + totalEojeolCnt.value + ", totalEojeolErrorCnt : " + totalEojeolErrorCnt.value + ", elapsed time : " + watch.getTime + " ms")

    println("eojeol accuracyRatio : " + eojeolAccuracyRatio + ", totalEojeolErrorCnt : " + totalEojeolErrorCnt.value + ", totalEojeolCnt : " + totalEojeolCnt.value)
    println("morph accuracyRatio : " + morphAccuracyRatio + ", totalMorphErrorCnt : " + totalMorphErrorCnt.value + ", totalMorphCnt : " + totalMorphCnt.value)
  }

  private def addRatio(correctRatio: Float) = {
    ratioArr += correctRatio
  }

  private def analyze(sentence: String) = {
    daonAnalyzer.analyzeText(sentence)
  }


  private def checkCandidate(correct: ArrayBuffer[Long], analyzed: ArrayBuffer[Int]) = {
    var errorCnt = 0

    correct.indices.foreach(i=>{
      val a = correct(i)
      var b = -1

      if(i < analyzed.size){
        b = analyzed(i)
      }

      if(a != b){
        errorCnt += 1
      }

    })

    errorCnt
  }

  private def check(correct: ArrayBuffer[Keyword], analyzed: ArrayBuffer[Keyword]) = {
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
