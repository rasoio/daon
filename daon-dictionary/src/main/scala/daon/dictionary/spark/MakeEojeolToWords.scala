package daon.dictionary.spark

import java.util.regex.Pattern

import org.apache.spark.sql._

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._

object MakeEojeolToWords {

  def main(args: Array[String]) {


    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
      .config("es.nodes", "localhost")
      .config("es.port", "9200")
      .config("es.index.auto.create", "true")
      .getOrCreate()

    write(spark)
//    read(spark)

  }

  case class Word(surface: String, wordSeqs: ArrayBuffer[Long])

  private def read(spark: SparkSession) = {
    import spark.implicits._

    val words = spark.read.json("/Users/mac/work/corpus/words")

    words.show()
    words.cache()
    words.createOrReplaceTempView("words")


    val wordDf = spark.sql(
      """
      select surface, wordSeqs, count(*) cnt
        from words
        group by surface, wordSeqs
        order by surface asc
      """)

    wordDf.coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/words_agg")
  }


  private def write(spark: SparkSession) = {
    import spark.implicits._

    val options = Map("es.read.field.exclude" -> "word_seqs")

    val df = spark.read.format("es").options(options).load("corpus/sentences")


    val wordsDf = df.flatMap(row => {
      val sentence = row.getAs[String]("sentence")
      val eojeols = row.getAs[Seq[Row]]("eojeols")


      val words = ArrayBuffer[Word]()

      eojeols.indices.foreach(e=> {
        val eojeol = eojeols(e)
        val surface_org = eojeol.getAs[String]("surface")
        val morphemes = eojeol.getAs[Seq[Row]]("morphemes")

        var surface = surface_org

        val results = surface.split("[^가-힣ㄱ-ㅎㅏ-ㅣ]")

        val flag = addWords(results, morphemes, words)


        if(flag){
          println("erro find ====>", sentence, surface_org)
        }

        /*
        var chk = true

        val wordSeqs = ArrayBuffer[Long]()

        morphemes.indices.foreach(m=>{
          val morpheme = morphemes(m)
          val seq = morpheme.getAs[Long]("seq")
          val w = morpheme.getAs[String]("word")
          val tag = morpheme.getAs[String]("tag")

          if(seq == 0){
            chk = false
          }

          if(tag.startsWith("S") && tag != "SF") {
            surface = surface.replaceFirst(Pattern.quote(w), "")
          }else{
            wordSeqs += seq
          }

        })

        if(wordSeqs.size <= 1){
          chk = false
        }

        if(chk) {

          //말뭉치 오류 검증용
          if(surface == "있다." && wordSeqs(0) == 29548){

            println(sentence, surface_org, surface, wordSeqs)
          }

          val word = Word(surface, wordSeqs)
          words += word
        }
        */

      })

//      println(words)

      words
    }).as[Word]


//    new_df.collect().take(10).foreach(println)
//    new_df.coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/words")


//    wordsDf.show()
//    wordsDf.cache()
    wordsDf.createOrReplaceTempView("words")


    val wordDf = spark.sql(
      """
      select surface, wordSeqs, count(*) cnt
        from words
        group by surface, wordSeqs
        order by surface asc
      """)

    wordDf.coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/words_agg")

  }


  private def addWords(results: Array[String], morphemes: Seq[Row], words: ArrayBuffer[Word]): Boolean ={

    var lastIdx = 0
    for (surface <- results) {

      val wordSeqs = ArrayBuffer[Long]()

      breakable {
        for (i <- lastIdx until morphemes.size) {

          val morpheme = morphemes(i)
          val seq = morpheme.getAs[Long]("seq")
          val w = morpheme.getAs[String]("word")
          val tag = morpheme.getAs[String]("tag")

          if (seq == 0 || tag.startsWith("S")) {
            lastIdx = i
            break
          }

          wordSeqs += seq


        }
      }

      if(surface.length > 0 && wordSeqs.size > 1) {

        //말뭉치 오류 검증용
//        if(surface == "있다" && wordSeqs(0) == 29548){
//
//          return true
//        }

        val word = Word(surface, wordSeqs)
        words += word
      }

    }

    return false
  }
}
