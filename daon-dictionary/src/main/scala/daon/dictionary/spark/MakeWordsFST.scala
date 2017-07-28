package daon.dictionary.spark

import java.io.{File, FileOutputStream, OutputStream}
import java.util
import java.util.Collections

import com.google.protobuf.ByteString
import daon.analysis.ko.fst.DaonFSTBuilder
import daon.analysis.ko.model._
import daon.analysis.ko.proto.Model
import daon.dictionary.spark.PreProcess.{Morpheme, Sentence, Word}
import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.spark.sql._

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}

object MakeWordsFST {

  case class PartialWords(surface: String, wordSeqs: Array[Int], freq: Long)
  case class PartialWordsTemp(surface: String, wordSeqs: ArrayBuffer[Int], direction: String = "" )

  val ERROR_SURFACE = "ERROR_SURFACE"

  private var dictionaryMap = new util.HashMap[Integer, Model.Keyword]()

  val isPrint = false


  val logFile = new File("/Users/mac/work/corpus/word.log")
  //initialize
  FileUtils.write(logFile, "", "UTF-8")
  var out = new FileOutputStream(logFile, true)

  def main(args: Array[String]) {


    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
//      .master("spark://daon.spark:7077")
      .config("es.nodes", "daon.es")
      .config("es.port", "9200")
      .config("es.index.auto.create", "true")
      //set new runtime options
      .config("spark.sql.shuffle.partitions", 4)
      .config("spark.executor.memory", "2g")
      .config("spark.driver.memory", "4g")
      .getOrCreate()

    val processedData = PreProcess.process(spark)

    val rawSentenceDF: Dataset[Sentence] = processedData.rawSentences

    val wordDF: Dataset[Word] = processedData.words

    makeFST(spark, rawSentenceDF, wordDF)

//    IOUtils.closeQuietly(out)

//    val morphemes = Seq(
//      Morpheme(1, "알", "VV"),
//      Morpheme(2, "시", "EP"),
//      Morpheme(3, "죠", "EF"),
//      Morpheme(4, "?", "SF"),
//      Morpheme(5, "\"", "SP")
//    )
//    val surface = "아시죠?\""

//    val morphemes = Seq(
//      Morpheme(1, "진행", "NNG"),
//      Morpheme(2, "되", "VV"),
//      Morpheme(3, "ㄹ", "EF")
//    )
//    val surface = "진행될"


//    val morphemes = Seq(
//      Morpheme(1, "돌아가", "NNG"),
//      Morpheme(2, "아", "VV"),
//      Morpheme(3, "달", "EF"),
//      Morpheme(4, "라고", "EF")
//    )
//    val surface = "돌아가달라고"

//    val morphemes = Seq(
//      Morpheme(1, "꺼내", "VV"),
//      Morpheme(2, "어", "EC")
//    )
//    val surface = "꺼내"

//    val morphemes = Seq(
//      Morpheme(1, "진한영", "NNP"),
//      Morpheme(2, "(", "SP"),
//      Morpheme(3, "양천여고", "NNP")
//    )
//    val surface = "진한영(양천여고"

//    val morphemes = Seq(
//      Morpheme(1, "주의", "NNG"),
//      Morpheme(2, "(", "SP"),
//      Morpheme(3, "attention", "SL"),
//      Morpheme(4, ")", "SP"),
//      Morpheme(5, "이", "VCP"),
//      Morpheme(6, "란", "ETM")
//    )
//    val surface = "주의(attention)란"

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
//    val surface = "(A)도식(민족주의)과"

//    val morphemes = Seq(
//      Morpheme(1, "5", "SP"),
//      Morpheme(2, ",", "SL"),
//      Morpheme(3, "000", "SP"),
//      Morpheme(4, "엔", "NNG"),
//      Morpheme(5, ",", "SP")
//    )
//    val surface = "5,000엔,"



//    val morphemes = Seq(
//      Morpheme(1, "불러내", "VV"),
//      Morpheme(3, "가", "VX"),
//      Morpheme(4, "잖어", "EF")
//    )
//    val surface = "불러내가잖어"

//    val morphemes = Seq(
//      Morpheme(1, "불러내", "VV"),
//      Morpheme(2, "어", "EC"),
//      Morpheme(3, "가", "VX"),
//      Morpheme(4, "잖어", "EF")
//    )
//    val surface = "불러내가잖어."

//    val morphemes = Seq(
//      Morpheme(1, "'", "SP"),
//      Morpheme(2, "朝鮮國太白山端宗大王之碑", "SH"),
//      Morpheme(3, "'", "SP"),
//      Morpheme(4, "이", "VCP"),
//      Morpheme(5, "라", "EC")
//    )
//    val surface = "'朝鮮國太白山端宗大王之碑'라"

//    val results = parsePartialWords(morphemes, surface)
//
//    results.foreach(println)

  }

  def makeFST(spark: SparkSession, rawSentenceDF: Dataset[Sentence], wordDF: Dataset[Word]): (ByteString, ByteString) = {
    //사전 단어
    val words = wordDF.collect()

    dictionaryMap = makeDictionaryMap(words)

    val partialWords = makePartialWords(spark, rawSentenceDF)
    val forwardPartialWords = partialWords._1
    val backwardPartialWords = partialWords._2

    val forwardKeywordIntsRefs = makeForwardKeywordIntsRefs(words, forwardPartialWords)
    val backwardKeywordIntsRefs = makeBackwardKeywordIntsRefs(words, backwardPartialWords)

    //빌드 fst
    val forwardFst = DaonFSTBuilder.create.buildPairFst(forwardKeywordIntsRefs)
    val forwardFstByte = DaonFSTBuilder.toByteString(forwardFst)

    println("forward words size : " + forwardKeywordIntsRefs.size() + ", ram used : " + forwardFst.getInternalFST.ramBytesUsed() + ", byte : " + forwardFstByte.size())

    //빌드 fst
    val backwardFst = DaonFSTBuilder.create.buildPairFst(backwardKeywordIntsRefs)
    val backwardFstByte = DaonFSTBuilder.toByteString(backwardFst)

    println("backward words size : " + backwardKeywordIntsRefs.size() + ", ram used : " + backwardFst.getInternalFST.ramBytesUsed() + ", byte : " + backwardFstByte.size())

    (forwardFstByte, backwardFstByte)
  }

  def getDictionaryMap: util.HashMap[Integer, Model.Keyword] = {
    dictionaryMap
  }


  private def makeDictionaryMap(words: Array[Word]) = {

    val dictionaryMap = new util.HashMap[Integer, Model.Keyword]()

    words.foreach(keyword => {

      val seq = keyword.seq
      //model dictionary 용
      val newKeyword = daon.analysis.ko.proto.Model.Keyword.newBuilder.setSeq(seq).setWord(keyword.word).setTag(keyword.tag).setFreq(keyword.freq).setDesc("").build
      dictionaryMap.put(seq, newKeyword)

//      println(keyword.seq, keyword.word, keyword.tag)
    })

    dictionaryMap
  }


  private def makeForwardKeywordIntsRefs(words: Array[Word], fowardPartialWords: Array[PartialWords]): util.ArrayList[KeywordIntsRef] = {

    val keywordIntsRefs = new util.ArrayList[KeywordIntsRef]

//    println("words count : " + partialWords.count())
//    println("partialWords count : " + partialWords.count())

    //사전
    words.filter(w=>w.tag.startsWith("S")).foreach(w => {
      val seq = w.seq
      val word = w.word
      val freq = w.freq

      val keywordIntsRef = new KeywordIntsRef(word, Array(seq))
      keywordIntsRef.setFreq(freq)
      keywordIntsRefs.add(keywordIntsRef)
    })

    //어절 부분 사전
    fowardPartialWords.foreach(w => {
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



  private def makeBackwardKeywordIntsRefs(words: Array[Word], backwardPartialWords: Array[PartialWords]): util.ArrayList[KeywordIntsRef] = {

    val keywordIntsRefs = new util.ArrayList[KeywordIntsRef]

    //어절 부분 사전
    backwardPartialWords.foreach(w => {
      val word = w.surface.reverse
      val seqs = w.wordSeqs
      val freq = w.freq

      val keywordIntsRef = new KeywordIntsRef(word, seqs)
      keywordIntsRef.setFreq(freq)
      keywordIntsRefs.add(keywordIntsRef)
    })

    Collections.sort(keywordIntsRefs)

    keywordIntsRefs
  }

  private def makePartialWords(spark: SparkSession, rawSentenceDF: Dataset[Sentence]): (Array[PartialWords], Array[PartialWords]) = {
    import spark.implicits._

    val partialWordsDf = rawSentenceDF.flatMap(row => {
      val sentence = row.sentence
      val eojeols = row.eojeols

      var words = ArrayBuffer[PartialWordsTemp]()

      eojeols.indices.foreach(e=> {
        val eojeol = eojeols(e)
        val surface = eojeol.surface
        val morphemes = eojeol.morphemes

        words ++= parsePartialWords(morphemes, surface)
      })


      if(isPrint) {
        println(sentence)
        words.foreach(w => {
          println(w.surface + " -> " + w.wordSeqs.map(seq => {
//            println(seq)
            val k = dictionaryMap.get(seq)

            if(k != null){
              val s = k.getSeq
              val w = k.getWord
              val t = k.getTag
              s"($s:$w-$t)"
            }else{
              s"($seq)"
            }

          }).mkString(", "))
        })
      }

      words
    }).as[PartialWordsTemp]

    partialWordsDf.createOrReplaceTempView("partial_words")
    partialWordsDf.cache()

    val forwardPartialWords = spark.sql(
      """
      select surface, wordSeqs, count(*) freq
        from partial_words
        where direction = 'f'
        group by surface, wordSeqs
        order by surface asc
      """).as[PartialWords]

    forwardPartialWords.cache()
    val forwardResults = forwardPartialWords.collect()


    val backwardPartialWords = spark.sql(
      """
      select surface, wordSeqs, count(*) freq
        from partial_words
        where direction = 'b'
        group by surface, wordSeqs
        order by surface asc
      """).as[PartialWords]

    backwardPartialWords.cache()
    val backwardResults = backwardPartialWords.collect()

    forwardPartialWords.unpersist()
    backwardPartialWords.unpersist()
    partialWordsDf.unpersist()
//    val results = partialWords.as[PartialWords]

//    results.coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/partial_words")

    (forwardResults, backwardResults)
  }


  private def parsePartialWords(morphemes: Seq[Morpheme], surface: String): ArrayBuffer[PartialWordsTemp] = {
    var words = ArrayBuffer[PartialWordsTemp]()

    // 소스 리펙토링 필요
    write("surface : " + surface + " :: morph : " + morphemes.map(m=>m.word + "/" + m.tag).mkString(","))

    var headMorp = morphemes
    var headSurface = surface
    var leftSurface = surface

    while(headMorp.nonEmpty){
      breakable {

        var head1 = headMorp.takeWhile(m => !isSplitTag(m.tag))

        val head2 = headMorp.takeWhile(m => isSplitTag(m.tag))

        //특수기호인 경우
        if (head1.isEmpty && head2.nonEmpty) {
          val last = head2.size
          val end = headMorp.size
          headMorp = headMorp.slice(last, end)

          val words = head2.map(w=>w.word).mkString
          val len = words.length

          //특수문자 정보 remove =>
          headSurface = if(len > leftSurface.length){
            ERROR_SURFACE
          }else{
            leftSurface.substring(len)
          }

          break
        }

        val lst = if(headMorp.size > head1.size){
          headMorp(head1.size)
        }else{
          headMorp.last
        }

        headSurface = if (isSplitTag(lst.tag)) {
          val w = lst.word
          val splitSurface = getSurface(headSurface, w)

          leftSurface = splitSurface._2

          val last = head1.size + 1
          val end = headMorp.size
          headMorp = headMorp.slice(last, end)

          splitSurface._1
        } else {

          val last = head1.size
          val end = headMorp.size
          headMorp = headMorp.slice(last, end)

          headSurface
        }

        val headMorpWords = head1.map(m => m.word).mkString("")

        val isIrrgular = headSurface != headMorpWords


        //불규칙 조건 설정
        val head = if (isIrrgular) {

          if (headSurface == ERROR_SURFACE) {
            return ArrayBuffer[PartialWordsTemp]()
          }

          var lstIdx = 0
          //되어버린 안됨...

          //매칭 된 어절 위치까지
          val at = head1.takeWhile(m => {
            val w = m.word
            val len = w.length

            val isMatch = headSurface.regionMatches(lstIdx, w, 0, len)

            //매칭 어절 idx 기록
            if (isMatch) lstIdx += w.length

            isMatch
          }).size

          val r = head1.splitAt(at)
          val s1 = headSurface.substring(0, lstIdx)
          val s2 = headSurface.substring(lstIdx)

          val nr = r._1.map(m => {
            val p = PartialWordsTemp(m.word, ArrayBuffer[Int](m.seq))
            ArrayBuffer[PartialWordsTemp](p)
          })

          val wordSeqs = r._2.map(m => m.seq).toArray
          val irr = ArrayBuffer[PartialWordsTemp](PartialWordsTemp(s2, ArrayBuffer(wordSeqs: _*)))

          nr :+ irr
        } else {
          head1.map(m => {
            val p = PartialWordsTemp(m.word, ArrayBuffer[Int](m.seq))
            ArrayBuffer[PartialWordsTemp](p)
          })
        }

        headSurface = leftSurface

        if (head.nonEmpty) {

//          println("surface : " + surface + " :: morph : " + morphemes.map(m=>m.word + "/" + m.tag).mkString(","))
//          println("headSurface : " + headSurface + " :: headMorpWords : " + headMorpWords + ", irr : " + (isIrrgular))

          write("headSurface : " + headSurface + " :: headMorpWords : " + headMorpWords + ", irr : " + (isIrrgular))

          val a = ArrayBuffer[PartialWordsTemp]()

          val lf = head.scanLeft(a)(_ ++ _).drop(1)
          val rf = head.scanRight(a)(_ ++ _).drop(1).dropRight(1)

          //앞 어절
          lf.foreach(c => {
            val s = c.map(w => w.surface).mkString("")
            val wordSeqs = c.flatMap(w => w.wordSeqs)

//            println("left : s = " + s + ", wordSeqs = " + wordSeqs.map(seq=>{
//              val k = dictionaryMap.get(seq)
//              if(k != null){
//                k.getWord + "/" + k.getTag
//              }else{
//                seq
//              }
//            }).mkString(","))

            write("left : s = " + s + ", wordSeqs = " + wordSeqs.map(seq=>{
              val k = dictionaryMap.get(seq)
              k.getWord + "/" + k.getTag
            }).mkString(","))

            if(s.nonEmpty && wordSeqs.nonEmpty) {
              words += PartialWordsTemp(s, wordSeqs, "f")
            }
          })

          //뒷 어절
          rf.foreach(c => {
            val s = c.map(w => w.surface).mkString("")
            val wordSeqs = c.flatMap(w => w.wordSeqs)

//            println("right : s = " + s + ", wordSeqs = " + wordSeqs.map(seq=>{
//              val k = dictionaryMap.get(seq)
//              if(k != null){
//                k.getWord + "/" + k.getTag
//              }else{
//                seq
//              }
//            }).mkString(","))

            write("right : s = " + s + ", wordSeqs = " + wordSeqs.map(seq=>{
              val k = dictionaryMap.get(seq)
              k.getWord + "/" + k.getTag
            }).mkString(","))

            if(s.nonEmpty && wordSeqs.nonEmpty){
              words += PartialWordsTemp(s, wordSeqs, "b")
            }
          })

        }
      }
    }

    words
  }

  private def write(txt: String): Unit = {
    IOUtils.write(txt + System.lineSeparator, out, "UTF-8")
  }


  private def isSplitTag(tag: String): Boolean = {
    tag.startsWith("S") || tag == "NA"
  }

  private def getHeadSurface(surface: String, word: String): String = {

    getSurface(surface, word)._1
  }


  private def getLeftSurface(surface: String, word: String): String = {

    getSurface(surface, word)._2
  }

  private def getSurface(surface: String, word: String): (String, String) = {

    val chkSurface = surface.toLowerCase
    val chkWord = word.toLowerCase

    val idx = chkSurface.indexOf(chkWord)

    val end = idx

    //오매핑 오류... 어절에 존재하지 않은 word
    if(idx == -1){
      return (ERROR_SURFACE, ERROR_SURFACE)
    }

    val partialSurface = chkSurface.substring(0, end)
    val leftSurface = chkSurface.substring(end + word.length)

    (partialSurface, leftSurface) // 부분 surface, 남은 surface
  }
}
