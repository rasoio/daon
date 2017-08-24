package daon.spark

import java.io.{File, FileOutputStream}
import java.util
import java.util.Collections

import com.google.protobuf.ByteString
import daon.analysis.ko.config.CharType
import daon.analysis.ko.fst.DaonFSTBuilder
import daon.analysis.ko.model._
import daon.analysis.ko.proto.Model
import daon.analysis.ko.util.{CharTypeChecker, Utils}
import PreProcess.{Morpheme, Sentence, Word}
import daon.spark.MakeWordsFST.{getSurface, isKorean, isSplitTag}
//import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.spark.sql._

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}

object MakeWordsFST {

  case class PartialWords(surface: String, wordSeqs: Array[Int], freq: Long)
  case class PartialWordsTemp(surface: String, wordSeqs: ArrayBuffer[Int], direction: String = "" )

  val WEIGHT = 200

  var maxFreq = 10000000f

  val ERROR_SURFACE = "ERROR_SURFACE"

  var dictionaryMap = new util.HashMap[Integer, Model.Keyword]()

//  val logFile = new File("/Users/mac/work/corpus/word.log")
  //initialize
//  FileUtils.write(logFile, "", "UTF-8")

//  var out = new FileOutputStream(logFile, true)

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
//      .master("spark://daon.spark:7077")
      .config("es.nodes", "localhost")
      .config("es.port", "9200")
      //set new runtime options
//      .config("spark.sql.shuffle.partitions", 4)
//      .config("spark.executor.memory", "2g")
      .getOrCreate()

    val processedData = PreProcess.process(spark)

    val rawSentenceDF: Dataset[Sentence] = processedData.rawSentences

    val wordDF: Dataset[Word] = processedData.words

    makeFST(spark, rawSentenceDF, wordDF)

  }

  def makeFST(spark: SparkSession, rawSentenceDF: Dataset[Sentence], wordDF: Dataset[Word]): ByteString = {
    //사전 단어
    val words = wordDF.collect()

    dictionaryMap = makeDictionaryMap(words)

    val partialWords = makePartialWords(spark, rawSentenceDF)

    val keywordIntsRefs = makeKeywordIntsRefs(words, partialWords)

    //빌드 fst
    val fst = DaonFSTBuilder.create.buildPairFst(keywordIntsRefs)
    val fstByte = DaonFSTBuilder.toByteString(fst)

//    println("words size : " + keywordIntsRefs.size() + ", ram used : " + fst.getInternalFST.ramBytesUsed() + ", byte : " + fstByte.size())

    wordDF.unpersist()
    rawSentenceDF.unpersist()

    fstByte
  }

  def getDictionaryMap: util.HashMap[Integer, Model.Keyword] = {
    dictionaryMap
  }


  private def makeDictionaryMap(words: Array[Word]) = {

    val dictionaryMap = new util.HashMap[Integer, Model.Keyword]()

    words.foreach(keyword => {

      val seq = keyword.seq
      //model dictionary 용
      val newKeyword = daon.analysis.ko.proto.Model.Keyword.newBuilder.setSeq(seq).setWord(keyword.word).setTag(keyword.tag).build
      dictionaryMap.put(seq, newKeyword)

//      println(keyword.seq, keyword.word, keyword.tag)
    })

    dictionaryMap
  }


  private def makeKeywordIntsRefs(words: Array[Word], partialWords: Array[PartialWords]): util.ArrayList[KeywordIntsRef] = {

    val keywordIntsRefs = new util.ArrayList[KeywordIntsRef]

//    println("words count : " + partialWords.count())
//    println("partialWords count : " + partialWords.count())

    //확률값 계산 방법 개선 필요

    //사전
    words.filter(w=>w.tag.startsWith("S")).foreach(w => {
      val seq = w.seq
      val word = w.word
      val p = w.freq / maxFreq
      val cost = toCost(p)

      val keywordIntsRef = new KeywordIntsRef(word, Array(seq))
      keywordIntsRef.setCost(cost)
      keywordIntsRefs.add(keywordIntsRef)
    })

    //어절 부분 사전
    partialWords.foreach(w => {
      val word = w.surface
      val seqs = w.wordSeqs
      val p = w.freq / maxFreq
      val cost = toCost(p)

      val keywordIntsRef = new KeywordIntsRef(word, seqs)
      keywordIntsRef.setCost(cost)
      keywordIntsRefs.add(keywordIntsRef)
    })

    Collections.sort(keywordIntsRefs)

    keywordIntsRefs
  }

  private def makePartialWords(spark: SparkSession, rawSentenceDF: Dataset[Sentence]): Array[PartialWords] = {
    import spark.implicits._

    val partialWordsDf = rawSentenceDF.flatMap(row => {
      val eojeols = row.eojeols

      var words = ArrayBuffer[PartialWordsTemp]()

      eojeols.indices.foreach(e=> {
        val eojeol = eojeols(e)
        val surface = eojeol.surface
        val morphemes = eojeol.morphemes

        words ++= parsePartialWords2(morphemes, surface)
      })

      words
    }).as[PartialWordsTemp]

    partialWordsDf.createOrReplaceTempView("partial_words")
    partialWordsDf.cache()

    val partialWords = spark.sql(
      """
      select surface, wordSeqs, count(*) freq
        from partial_words
        group by surface, wordSeqs
        order by surface asc
      """).as[PartialWords]

    partialWords.cache()

    maxFreq = partialWords.groupBy().max("freq").collect()(0).getLong(0).toFloat

//    partialWords.coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/partial_words")

    val results = partialWords.collect()

    partialWords.unpersist()
    partialWordsDf.unpersist()

    results
  }


  def parsePartialWords(morphemes: Seq[Morpheme], surface: String): ArrayBuffer[PartialWordsTemp] = {
    var words = ArrayBuffer[PartialWordsTemp]()

    // 소스 리펙토링 필요..
    var headMorp = morphemes
    var headSurface = surface
    var leftSurface = surface

    while(headMorp.nonEmpty){
      breakable {

        val head1 = headMorp.takeWhile(m => !isSplitTag(m.tag))

        val head2 = headMorp.takeWhile(m => isSplitTag(m.tag))

        //앞 부분이 특수기호인 경우 제외 처리
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
          //특수문자 형태소 위치
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

          // 특문, 영문, 숫자 제외 처리 필요
          if(!isKorean(headSurface)){
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

          var nr = r._1.map(m => {
            val p = PartialWordsTemp(m.word, ArrayBuffer[Int](m.seq))
            ArrayBuffer[PartialWordsTemp](p)
          })

          val wordSeqs = r._2.map(m => m.seq).toArray
          //불규칙 결과가 존재할경우만
          if(wordSeqs.length > 0) {
            val irr = ArrayBuffer[PartialWordsTemp](PartialWordsTemp(s2, ArrayBuffer(wordSeqs: _*)))
            nr :+= irr
          }

          nr
        } else {
          head1.filter(m => {
            isKorean(m.word)
          }).map(m => {
            val p = PartialWordsTemp(m.word, ArrayBuffer[Int](m.seq))
            ArrayBuffer[PartialWordsTemp](p)
          })
        }

        headSurface = leftSurface

        if (head.nonEmpty) {

//          println("surface : " + surface + " :: morph : " + morphemes.map(m=>m.word + "/" + m.tag).mkString(","))
//          println("headSurface : " + headSurface + " :: headMorpWords : " + headMorpWords + ", irr : " + (isIrrgular))

//          write("headSurface : " + headSurface + " :: headMorpWords : " + headMorpWords + ", irr : " + (isIrrgular))

          val a = ArrayBuffer[PartialWordsTemp]()

          val lf = head.scanLeft(a)(_ ++ _).drop(1)
          val rf = head.scanRight(a)(_ ++ _).drop(1).dropRight(1)

          //앞 어절
          lf.foreach(c => {
            val s = c.map(w => w.surface).mkString("")
            val wordSeqs = c.flatMap(w => w.wordSeqs)

            if(s.nonEmpty && wordSeqs.nonEmpty) {

              words += PartialWordsTemp(s, wordSeqs, "f")
            }
          })

          //뒷 어절
          rf.foreach(c => {
            val s = c.map(w => w.surface).mkString("")
            val wordSeqs = c.flatMap(w => w.wordSeqs)

            if(s.nonEmpty && wordSeqs.nonEmpty){

              words += PartialWordsTemp(s, wordSeqs, "b")
            }
          })

        }
      }
    }

    words
  }


  def parsePartialWords2(morphemes: Seq[Morpheme], s: String): ArrayBuffer[PartialWordsTemp] = {
    //partial words 추출 결과
    var words = ArrayBuffer[PartialWordsTemp]()

    //형태소 찾기 시작 위치
    var fromIndex = 0
    //부분 어절 분리 시작 위치
    var offset = 0

    var seqBuffer = ArrayBuffer[Int]()

    var partialResults = ArrayBuffer[ArrayBuffer[PartialWordsTemp]]()

    //특수문자 형태소의 word 가 surface 에 누락된 경우
    val surface = s.toLowerCase

    //surface 의 특수문자가 morphemes 에 누락된 경우
    breakable {
      morphemes.foreach(m => {
        val seq = m.seq
        val word = m.word.toLowerCase
        val tag = m.tag
        var length = word.length

        val isKorWord = isKorean(word)

        var findOffset = surface.indexOf(word, fromIndex)

        if (findOffset > -1) {
          //        println(s"findOffset : $findOffset, offset : $offset, length : $length, p : $p")

          //불러내가잖어 와 같이 어 가 두번 들어가는 케이스일때...
          if (isKorWord && seqBuffer.isEmpty && findOffset > fromIndex) {
            val prev = surface.substring(fromIndex, findOffset)

            if (isKorean(prev)) {
              findOffset = -1
            }
          }
        }

        val isExist = findOffset > -1

        //특문인 경우
        if (!isKorWord || isSplitTag(tag)) {

          if (isExist && seqBuffer.nonEmpty) {
            val bword = surface.substring(offset, findOffset)
            partialResults += ArrayBuffer(PartialWordsTemp(bword, seqBuffer))

            seqBuffer = ArrayBuffer[Int]()
          }

          if (isExist) {
            fromIndex = findOffset + length
          } else {

            //특문인데 존재하지 않는 경우...
            seqBuffer = ArrayBuffer[Int]()
            partialResults = ArrayBuffer[ArrayBuffer[PartialWordsTemp]]()

            break
          }

          offset = fromIndex

          addWords(words, partialResults)
          partialResults = ArrayBuffer[ArrayBuffer[PartialWordsTemp]]()

        } else {
          if (isExist) {

            var partialSeqBuffer = ArrayBuffer[Int]()
            if (seqBuffer.nonEmpty) {

              val bword = surface.substring(offset, findOffset)
              val blength = bword.length

              //            println(s"nonEmpty => offset : $offset, buffer(seq) : $seqBuffer, word : $bword")

              //head add
              if (blength == 0) {
                if (partialResults.isEmpty) {
                  partialSeqBuffer ++= seqBuffer
                } else {
                  partialResults.last.last.wordSeqs ++= seqBuffer
                }
              } else {
                //              length += blength
                partialResults += ArrayBuffer(PartialWordsTemp(bword, seqBuffer))
              }

              //clear buffer
              seqBuffer = ArrayBuffer[Int]()
            }

            val pw = if (partialSeqBuffer.nonEmpty) {
              partialSeqBuffer += seq
              PartialWordsTemp(word, partialSeqBuffer)
            } else {
              PartialWordsTemp(word, ArrayBuffer[Int](seq))
            }

            partialResults += ArrayBuffer(pw)

            fromIndex = findOffset + length

            offset = fromIndex
            //          println(s"isExist : $isExist, findOffset: $findOffset, offset : $offset, seq : $seq, word : $word, tag : $tag, surface : $surface")
          } else {

            //불규칙 이후 매칭 되는 surface offset 계산 방안..
            seqBuffer += seq
            //임시 처리..
            fromIndex += length - 1
            //          println(s"isExist : $isExist, offset : $offset, seq : $seq, word : $word, tag : $tag, surface : $surface")

          }
        }
      })
    }


    //flush
    if(seqBuffer.nonEmpty){
      //예외처리 필요 => 오매핑 상태인 경우
      if(fromIndex > surface.length){
//        val morphStr = morphemes.map(m=>m.word).mkString(",")
//        write(s"=======> surface : $surface, morpheme : $morphStr")
//        write(s"surface : $surface, words : $words")
        partialResults = ArrayBuffer[ArrayBuffer[PartialWordsTemp]]()
      }else{
        val word = surface.substring(offset)
        if(word.nonEmpty && isKorean(word)){
          partialResults += ArrayBuffer(PartialWordsTemp(word, seqBuffer))
        }else{
          if(partialResults.nonEmpty){
            partialResults.last.last.wordSeqs ++= seqBuffer
          }
        }
      }
    }
//    println(partialResults)

    addWords(words, partialResults)

//    write(s"surface : $surface, words : $words")
    words
  }


  private def addWords(words: ArrayBuffer[PartialWordsTemp], partialResults: ArrayBuffer[ArrayBuffer[PartialWordsTemp]]): Unit = {
    //음...
    if (partialResults.nonEmpty) {

      val tmp = ArrayBuffer[PartialWordsTemp]()

      val lf = partialResults.scanLeft(tmp)(_ ++ _).drop(1)
      val rf = partialResults.scanRight(tmp)(_ ++ _).drop(1).dropRight(1)

      //앞 어절
      lf.foreach(c => {
        val s = c.map(w => w.surface).mkString("")
        val wordSeqs = c.flatMap(w => w.wordSeqs)

        if (s.nonEmpty && wordSeqs.nonEmpty) {

          words += PartialWordsTemp(s, wordSeqs, "f")
        }
      })

      //뒷 어절
      rf.foreach(c => {
        val s = c.map(w => w.surface).mkString("")
        val wordSeqs = c.flatMap(w => w.wordSeqs)

        if (s.nonEmpty && wordSeqs.nonEmpty) {

          words += PartialWordsTemp(s, wordSeqs, "b")
        }
      })

    }
  }

  private def isKorean(txt: String): Boolean = {
    val chars = txt.toCharArray

    chars.foreach(c => {
      val charType = CharTypeChecker.charType(c)
      if(charType != CharType.KOREAN && charType != CharType.JAMO){
        return false
      }
    })

    true
  }

  private def write(txt: String): Unit = {
//    IOUtils.write(txt + System.lineSeparator, out, "UTF-8")
  }

  private def isSplitTag(tag: String): Boolean = {
    tag.startsWith("S") || tag == "NA"
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

  private def toCost(p: Float) = {
    val w = WEIGHT
    val score = Math.log(p)
    (-w * score).toShort
  }
}
