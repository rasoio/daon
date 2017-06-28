package daon.dictionary.spark

import java.util
import java.util.Collections

import com.google.protobuf.ByteString
import daon.analysis.ko.fst.DaonFSTBuilder
import daon.analysis.ko.model._
import daon.analysis.ko.proto.Model
import org.apache.spark.sql._

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}

object MakeWordsFST {

  case class Word(seq: Long, word: String, tag: String, freq: Long, desc: String = "")

  case class PartialWords(surface: String, wordSeqs: Array[Int], freq: Long)
  case class PartialWordsTemp(surface: String, wordSeqs: ArrayBuffer[Int])

  val ERROR_SURFACE = "ERROR_SURFACE"

  private var dictionaryMap = new util.HashMap[Integer, Model.Keyword]()

  def main(args: Array[String]) {


    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
//      .master("spark://daon.spark:7077")
      .config("es.nodes", "daon.es")
      .config("es.port", "9200")
      .config("es.index.auto.create", "true")
      .getOrCreate()

    val rawSentenceDF: DataFrame = MakeModel.readSentences(spark)

    val wordDF: Dataset[Word] = MakeModel.readWords(spark)

    makeFST(spark, rawSentenceDF, wordDF)

  }

  def makeFST(spark: SparkSession, rawSentenceDF: Dataset[Row], wordDF: Dataset[Word]): ByteString = {
    //사전 단어
    val words = wordDF.collect()

    dictionaryMap = makeDictionaryMap(words)

    val partialWordsDF = makePartialWords(spark, rawSentenceDF)

    val keywordIntsRefs = makeKeywordIntsRefs(words, partialWordsDF)

    //빌드 fst
    val fst = DaonFSTBuilder.create.buildPairFst(keywordIntsRefs)
    val fstByte = DaonFSTBuilder.toByteString(fst)

    println("words size : " + keywordIntsRefs.size() + ", ram used : " + fst.getInternalFST.ramBytesUsed() + ", byte : " + fstByte.size())

    fstByte
  }

  def getDictionaryMap: util.HashMap[Integer, Model.Keyword] = {
    dictionaryMap
  }


  private def makeDictionaryMap(words: Array[Word]) = {

    val dictionaryMap = new util.HashMap[Integer, Model.Keyword]()

    words.foreach(keyword => {

      val seq = keyword.seq.toInt
      //model dictionary 용
      val newKeyword = daon.analysis.ko.proto.Model.Keyword.newBuilder.setSeq(seq).setWord(keyword.word).setTag(keyword.tag).setFreq(keyword.freq).setDesc("").build
      dictionaryMap.put(seq, newKeyword)

    })

    dictionaryMap
  }


  private def makeKeywordIntsRefs(words: Array[Word], partialWords: Dataset[PartialWords]): util.ArrayList[KeywordIntsRef] = {

    val keywordIntsRefs = new util.ArrayList[KeywordIntsRef]

    println("words count : " + partialWords.count())
    println("partialWords count : " + partialWords.count())

    //사전
    words.foreach(w => {
      val seq = w.seq.toInt
      val word = w.word
      val freq = w.freq

      val keywordIntsRef = new KeywordIntsRef(word, Array(seq))
      keywordIntsRef.setFreq(freq)
      keywordIntsRefs.add(keywordIntsRef)
    })

    //어절 부분 사전
    partialWords.collect().foreach(w => {
      val word = w.surface
      val seqs = w.wordSeqs
      val freq = w.freq

      val keywordIntsRef = new KeywordIntsRef(word, seqs)
      keywordIntsRef.setFreq(freq)
      keywordIntsRefs.add(keywordIntsRef)
    })

    Collections.sort(keywordIntsRefs)

    keywordIntsRefs
  }

  private def makePartialWords(spark: SparkSession, rawSentenceDF: DataFrame): Dataset[PartialWords] = {
    import spark.implicits._

    val partialWordsDf = rawSentenceDF.flatMap(row => {
      val sentence = row.getAs[String]("sentence")
      val eojeols = row.getAs[Seq[Row]]("eojeols")

      var words = ArrayBuffer[PartialWordsTemp]()

      eojeols.indices.foreach(e=> {
        val eojeol = eojeols(e)
        val surface = eojeol.getAs[String]("surface")
        val morphemes = eojeol.getAs[Seq[Row]]("morphemes")

        words ++= parsePartialWords(morphemes, surface)
      })


      println(sentence)
      words.foreach(w => {
        println(w.surface + " -> " + w.wordSeqs.map(seq=>{
          val k = dictionaryMap.get(seq)

          val s = k.getSeq
          val w = k.getWord
          val t = k.getTag

          s"($s:$w-$t)"
        }).mkString(", "))
      })

      words
    }).as[PartialWordsTemp]

    partialWordsDf.createOrReplaceTempView("partial_words")

    val partialWords = spark.sql(
      """
      select surface, wordSeqs, count(*) freq
        from partial_words
        group by surface, wordSeqs
        order by surface asc
      """)

    partialWords.cache()
    val results = partialWords.as[PartialWords]

    results.coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/partial_words")

    results
  }


  private def parsePartialWords(morphemes: Seq[Row], surface_org: String): ArrayBuffer[PartialWordsTemp] = {
    var words = ArrayBuffer[PartialWordsTemp]()
    var wordSeqs = ArrayBuffer[Int]()

    var surface = surface_org
    val last = morphemes.length - 1

    breakable {
      morphemes.indices.foreach(i => {

        val morpheme = morphemes(i)
        val seq = morpheme.getAs[Long]("seq").toInt
        val w = morpheme.getAs[String]("word")
        val tag = morpheme.getAs[String]("tag")

        // 숫자, 영문, 한자 특수문자인 경우 분리
        if (tag.startsWith("S") || tag == "NA") {

          val s = getSurface(surface, w)

          //오매칭 케이스인 경우 제외
          if (s._2 == ERROR_SURFACE) {
            words = ArrayBuffer[PartialWordsTemp]()
            break
          }

          val partialSurface = s._1
          surface = s._2

          if (partialSurface.length > 0 && wordSeqs.length > 1) {

            words += PartialWordsTemp(partialSurface, wordSeqs)
          }

          wordSeqs = ArrayBuffer[Int]()
        } else {
          wordSeqs += seq
        }

        //마지막
        if (i == last) {
          if (wordSeqs.length > 1) {
            words += PartialWordsTemp(surface, wordSeqs)
          }
        }

      })
    }

    words
  }

  private def getSurface(surface: String, word: String): (String, String) = {

    val chkSurface = surface.toLowerCase
    val chkWord = word.toLowerCase

    val idx = chkSurface.indexOf(chkWord)

    val end = idx

    //오매핑 오류... 어절에 존재하지 않은 word
    if(idx == -1){
      return ("", ERROR_SURFACE)
    }

    val partialSurface = chkSurface.substring(0, end)
    val leftSurface = chkSurface.substring(end + word.length)

    (partialSurface, leftSurface) // 부분 surface, 남은 surface
  }
}
