package daon.dictionary.spark

import daon.analysis.ko.DaonAnalyzer4
import daon.analysis.ko.model.loader.ModelLoader
import org.apache.commons.lang3.time.StopWatch
import org.apache.spark.sql._

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.ArrayBuffer

object EvaluateModel {

  val loader = ModelLoader.create.load
  val daonAnalyzer4 = new DaonAnalyzer4(loader.getFst, loader.getModel)
  var ratioArr = ArrayBuffer[Float]()


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

  }

  private def readEs(spark: SparkSession) = {
    import spark.implicits._

    val options = Map(
      "es.read.field.as.array.include" -> "word_seqs"
    )

    val df = spark.read.format("es").options(options).load("corpus/sentences")

//    val evaluateSet = df.take(100)
    val evaluateSet = df

//    df.printSchema()
//    df.createOrReplaceTempView("sentence")

    val watch = new StopWatch

    watch.start()

    daonAnalyzer4.setDebug(false)

//    var totalEojeolCnt = 0

    val totalEojeolCnt = spark.sparkContext.longAccumulator("totalEojeolCnt")
    val totalEojeolErrorCnt = spark.sparkContext.longAccumulator("totalEojeolErrorCnt")
    val ratioArr = spark.sparkContext.collectionAccumulator[Float]("ratioArr")

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

        val r_wordSeqs = ArrayBuffer[Int]()

        for ( term <- r_terms ) {
          for( seq <- term.getSeqs ){
            r_wordSeqs += seq
          }
        }

//        println(surface, r_surface)

        val wordSeqs = ArrayBuffer[Long]()

        morphemes.indices.foreach(m=>{
          val morpheme = morphemes(m)
          val seq = morpheme.getAs[Long]("seq")
          val w = morpheme.getAs[String]("word")
          val tag = morpheme.getAs[String]("tag")

          wordSeqs += seq
        })

//        println(wordSeqs, r_wordSeqs)
        val errorCnt = check(wordSeqs, r_wordSeqs)

        //정확률
        val totalCnt = wordSeqs.size
        val correctCnt = totalCnt - errorCnt

        val correctRatio = correctCnt.toFloat / totalCnt

        if(errorCnt > 0){
          // 에러 결과 별도 리포팅 필요
//          println(errorCnt, surface, getKeyword(wordSeqs, r_wordSeqs))

//          println(errorCnt, surface, wordSeqs, r_wordSeqs, totalEojeolErrorCnt, totalEojeolCnt)

          totalEojeolErrorCnt.add(1)
        }

//        println(correctRatio)

//        ratioArr += correctRatio


        ratioArr.add(correctRatio)
//        addRatio(correctRatio)
      })


    })

    watch.stop()

    val ratioSum = ratioArr.value.sum

    val avgRatio = ratioSum / totalEojeolCnt.value

    println("avgRatio : " + avgRatio + ", totalEojeolCnt : " + totalEojeolCnt.value + ", totalEojeolErrorCnt : " + totalEojeolErrorCnt.value + ", elapsed time : " + watch.getTime + " ms")
  }

  private def addRatio(correctRatio: Float) = {
    ratioArr += correctRatio
  }


  private def getKeyword(correct: ArrayBuffer[Long], analyzed: ArrayBuffer[Int]) = {

    var keywords = ArrayBuffer[String]()

    for(i <- correct){
      val keyword = daonAnalyzer4.getKeyword(i.toInt)

      if(keyword != null) {
        keywords += keyword.toString
      }
    }

    val correctKeywords = keywords.mkString(" : ")

    keywords = ArrayBuffer[String]()

    for(i <- analyzed){
      val keyword = daonAnalyzer4.getKeyword(i.toInt)

      if(keyword != null) {
        keywords += keyword.toString
      }
    }

    val analyzedKeywords = keywords.mkString(" : ")


    correctKeywords + " ||| " + analyzedKeywords
  }

  private def analyze(sentence: String) = {
    daonAnalyzer4.analyzeText(sentence)
  }


  private def check(correct: ArrayBuffer[Long], analyzed: ArrayBuffer[Int]) = {
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
}
