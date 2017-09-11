package daon.spark

import java.io.{File, FileOutputStream}
import java.util
import java.util.Collections

import com.google.protobuf.ByteString
import daon.core.config.CharType
import daon.core.fst.DaonFSTBuilder
import daon.core.model._
import daon.analysis.ko.proto.Model
import daon.core.util.{CharTypeChecker, Utils}
import daon.spark.PreProcess.{Morpheme, Sentence, Word}
import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.spark.sql._

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}

object MakeWordsFST {

  case class PartialWords(surface: String, wordSeqs: Array[Int], freq: Long)
  case class PartialWordsTemp(surface: String, wordSeqs: ArrayBuffer[Int], direction: String = "" )

  case class SurfaceMorphs(surface: String, morphs: ArrayBuffer[Morpheme])

  case class IrrMorphs(surface: String, morphs: ArrayBuffer[Morpheme])

  val WEIGHT = 200


//  706440
//  partialMaxFreq : 398754
//  partialMaxFreq : 86575
//  val logFile = new File("/Users/mac/work/corpus/word.log")
//  FileUtils.write(logFile, "", "UTF-8")
//  var out = new FileOutputStream(logFile, true)

//  var map = new util.HashMap[Integer, Model.Keyword]()

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
      .config("es.nodes", "localhost")
      .config("es.port", "9200")
      .getOrCreate()

    val processedData = PreProcess.process(spark)

    val rawSentenceDF: Dataset[Sentence] = processedData.rawSentences

    val wordDF: Array[Word] = processedData.words

    val maxFreq: Long = processedData.maxFreq

    println(s"maxFreq : ${maxFreq}")

    makeFST(spark, rawSentenceDF, wordDF, maxFreq)

  }

  def makeFST(spark: SparkSession, rawSentenceDF: Dataset[Sentence], words: Array[Word], maxFreq: Long): ByteString = {

//    map = makeDictionaryMap(words)

    val partialWords = makePartialWords(spark, rawSentenceDF)

    val keywordIntsRefs = makeKeywordIntsRefs(words, partialWords, maxFreq.toFloat)

    //빌드 fst
    val fst = DaonFSTBuilder.create.buildPairFst(keywordIntsRefs)
    val fstByte = DaonFSTBuilder.toByteString(fst)

    println(s"partialWords : ${partialWords.length} keywords size : ${keywordIntsRefs.size()}")

    rawSentenceDF.unpersist()

    fstByte
  }

  def makeDictionaryMap(words: Array[Word]): util.HashMap[Integer, Model.Keyword] = {

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


  private def makeKeywordIntsRefs(words: Array[Word], partialWords: Array[PartialWords], maxFreq: Float): util.ArrayList[KeywordIntsRef] = {

    val keywordIntsRefs = new util.ArrayList[KeywordIntsRef]

    println("words count : " + words.length)
    println("partialWords count : " + partialWords.length)

    //확률값 계산 방법 개선 필요

    //사전
    words.foreach(w => {
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

    val partialWordsTemp = rawSentenceDF.flatMap(row => {
      val eojeols = row.eojeols

      var words = ArrayBuffer[PartialWordsTemp]()

      eojeols.indices.foreach(e=> {
        val eojeol = eojeols(e)
        val surface = eojeol.surface
        val morphemes = eojeol.morphemes

        words ++= parsePartialWords(surface, morphemes)
      })

      words
    }).as[PartialWordsTemp]

    partialWordsTemp.createOrReplaceTempView("partial_words")
    partialWordsTemp.cache()

    val partialWords = spark.sql(
      """
      select surface, wordSeqs, count(*) freq
        from partial_words
        group by surface, wordSeqs
        order by surface asc
      """).as[PartialWords]

    partialWords.cache()

//    val partialMaxFreq = partialWords.groupBy().max("freq").collect()(0).getLong(0)
//    println(s"partialMaxFreq : ${partialMaxFreq}")
//    partialWords.coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/partial_words")

    val results = partialWords.collect()

//    results.foreach(w=>{
//      val surface = w.surface
//      val words = w.wordSeqs.map(s => {
//        val keyword = map.get(s)
//
//        val result = if(keyword == null){
//          "null"
//        }else{
//          keyword.getWord + "/" + keyword.getTag
//        }
//
//        result
//      }).mkString(",")
//
//      IOUtils.write(s"$surface => $words => ${w.freq}${System.lineSeparator}", out, "UTF-8")
//    })

    partialWords.unpersist()
    partialWordsTemp.unpersist()

    results
  }


  def parsePartialWords(surface: String, morphemes: Seq[Morpheme]): ArrayBuffer[PartialWordsTemp] = {
    //partial words 추출 결과
    var words = ArrayBuffer[PartialWordsTemp]()

    //특수문자 형태소의 word 가 surface 에 누락된 경우
    val s = surface.toLowerCase

    //surface 의 특수문자가 morphemes 에 누락된 경우
    val step1Results = step1(s, morphemes)

//    step1Results.foreach(println)

    step2(words, step1Results)

    //    write(s"surface : $surface, words : $words")
    words
  }

  /**
    * S 태그 값 제거 결과 리턴
    * @param surface
    * @param morphemes
    * @return
    */
  private def step1(surface: String, morphemes: Seq[Morpheme]): ArrayBuffer[SurfaceMorphs] = {
    var partialResults = ArrayBuffer[SurfaceMorphs]()

    var from = 0
    var beginIndex = 0

    morphemes.indices.foreach(m => {
      val morph = morphemes(m)
      val word = morph.word.toLowerCase
      val tag = morph.tag
      val length = word.length

      if(isRemoveTag(tag)){
        val offset = surface.indexOf(word, beginIndex)

        //특수문자가 포함안된 경우.. 처리 곤란
        if(offset == -1){
          return ArrayBuffer[SurfaceMorphs]()
        }

        val endIndex = offset
        val until = m

        if(endIndex > beginIndex) {
          val partialSurface = surface.substring(beginIndex, endIndex)
          val partialMorphs = morphemes.slice(from, until).to[ArrayBuffer]

          partialResults += SurfaceMorphs(partialSurface, partialMorphs)
        }

        from = m + 1
        beginIndex = offset + length
      }

    })

    //flush
    if(beginIndex < surface.length){
      val endIndex = surface.length
      val until = morphemes.size

      val partialSurface = surface.substring(beginIndex, endIndex)
      val partialMorphs = morphemes.slice(from, until).to[ArrayBuffer]

      partialResults += SurfaceMorphs(partialSurface, partialMorphs)
    }

    partialResults
  }

  /**
    * 불규칙 사전 구성 결과 리턴
    * @param step1Results
    * @return
    */
  private def step2(words: ArrayBuffer[PartialWordsTemp], step1Results: ArrayBuffer[SurfaceMorphs]): Unit = {

    step1Results.foreach(p => {
      val partialResults = ArrayBuffer[ArrayBuffer[SurfaceMorphs]]()


      val surface = p.surface
      val surfaceLength = surface.length
      val morphemes = p.morphs

      if(surfaceLength <= 2){
        partialResults += ArrayBuffer(SurfaceMorphs(surface, morphemes))

      }else{
        findPartialWords(surface, morphemes, partialResults)
      }

      addWords(words, partialResults)
    })
  }

  def findPartialWords(surface:String, morphemes: ArrayBuffer[Morpheme], partialResults: ArrayBuffer[ArrayBuffer[SurfaceMorphs]]): Unit = {

    val morphsLength = morphemes.length
    var seqBuffer = ArrayBuffer[Morpheme]()
    var beginIndex = 0

    var m = 0
    while (m < morphsLength) {
      val morph = morphemes(m)
      val seq = morph.seq
      val word = morph.word.toLowerCase
      val tag = morph.tag

      var length = word.length
      var size = 1

      val offset = indexOf(surface, beginIndex, morphemes, m)

      //존재하는 형태소 처리
      if (offset > -1) {

        val morphs = if (seqBuffer.isEmpty) {
          ArrayBuffer[Morpheme](morph)
        } else {
          seqBuffer += morph
        }

        partialResults += ArrayBuffer(SurfaceMorphs(word, morphs))

        //clear buffer
        seqBuffer = ArrayBuffer[Morpheme]()
      }
      //불규칙 처리
      else {
        val irrMorphs = findIrrMorphs(surface, beginIndex, morphemes, m)

        val irrWord = irrMorphs.surface
        val irrWordMorphs = irrMorphs.morphs

        length = irrWord.length
        size = irrWordMorphs.size

        if (irrWord.isEmpty) {
          if (partialResults.nonEmpty) {
            partialResults.last.last.morphs ++= irrWordMorphs
          } else {
            seqBuffer ++= irrWordMorphs
          }
        } else {

          //debugging
//          if ("가" == irrWord) {
//
//            var exist = false
//            irrWordMorphs.foreach(w => {
//
//              if (w.word == "ㄴ가" && w.tag == "EC")
//                exist = true
//            })
//
//            if (exist) {
//              println(s"${surface} => ${morphemes.map(w => w.word + "/" + w.tag).mkString(",")} : ${irrWord} : (${irrWordMorphs.map(w => w.word + "/" + w.tag).mkString(",")})")
//            }
//          }

          partialResults += ArrayBuffer(SurfaceMorphs(irrWord, irrWordMorphs))
        }
      }

      beginIndex += length
      m += size
    }
  }


  // 불규칙일때, 다음 단어까지 포함해서 찾기
  def findIrrMorphs(surface:String, beginIndex: Int, morphemes: ArrayBuffer[Morpheme], m: Int): IrrMorphs = {
    var matchSurface = ""

    val from = m
    val morphsLength = morphemes.length

    //맨뒤부터 매칭 단어 제거한 결과만 남김.
    val (source, removeCnt) = removeEndWith(surface, morphemes, m)

    val until = morphsLength - removeCnt

    //전체
    if(beginIndex < source.length){
      matchSurface = source.substring(beginIndex)
    }
    val irrMorphs = morphemes.slice(from, until)

    IrrMorphs(matchSurface, irrMorphs)
  }

  def removeEndWith(surface:String, morphemes: Seq[Morpheme], m: Int): (String, Int) = {

    var result = surface
    val length = result.length
    var endIndex = length
    var removeCnt = 0

    breakable {
      val start = morphemes.length - 1

      for (i <- start until m by -1) {
        val w = morphemes(i)
        val word = w.word
        val len = word.length

        if(result.endsWith(word)){
          removeCnt += 1
          endIndex -= len
          result = result.substring(0, endIndex)
        }else{
          break()
        }
      }
    }

    (result, removeCnt)
  }


  private def indexOf(surface: String, fromIndex: Int, morphemes: ArrayBuffer[Morpheme], m: Int): Int = {
    val morphsLength = morphemes.length
    val word = morphemes(m).word
    val length = word.length

    val findOffset = surface.indexOf(word, fromIndex)

    if(findOffset == fromIndex){
      return findOffset
    }

    //중간에 다른 문자가 존재하는 경우
    if (findOffset > fromIndex) {
      //다른 문자가 남아있는 형태소에 존재하는 경우는 잘못된 매칭
      val prev = surface.substring(fromIndex, findOffset + 1)

      for (i <- m + 1 until morphsLength) {
        val w = morphemes(i).word
        val offset = prev.indexOf(w)
        if (offset > -1) {
          return -1
        }
      }
      //그게 아닌 경우는 정상 처리
      return findOffset
    }

    findOffset
  }

  private def addWords(words: ArrayBuffer[PartialWordsTemp], partials: ArrayBuffer[ArrayBuffer[SurfaceMorphs]]): Unit = {
    if (partials.nonEmpty) {

      val partialResults = partials
//        .filterNot(p => {
//        val surface = p.map(w=>w.surface).mkString
//
//        //영문이나 숫자로만 이루어진 단어는 제외
//        isHanja(surface) || isDigit(surface) || isAlpha(surface)
//      })

      val tmp = ArrayBuffer[SurfaceMorphs]()

      val lf = partialResults.scanLeft(tmp)(_ ++ _).drop(1)
      val rf = partialResults.scanRight(tmp)(_ ++ _).drop(1).dropRight(1)

      //앞 어절
      lf.foreach(c => {
        val s = c.map(w => w.surface).mkString
        val morphs = c.flatMap(w => w.morphs)

        if(validatePartialWords(s, morphs)){
          val wordSeqs = morphs.map(w=>w.seq)
          words += PartialWordsTemp(s, wordSeqs, "f")
        }
      })

      //뒷 어절
      rf.foreach(c => {
        val s = c.map(w => w.surface).mkString
        val morphs = c.flatMap(w => w.morphs)

        if(validatePartialWords(s, morphs)){
          val wordSeqs = morphs.map(w=>w.seq)
          words += PartialWordsTemp(s, wordSeqs, "b")
        }
      })

    }
  }

  def validatePartialWords(surface: String, morphemes: Seq[Morpheme]): Boolean = {
    if(surface.isEmpty || morphemes.isEmpty){
      return false
    }

    if(morphemes.size == 1){
      return false
    }

    if(surface.length == 1){
      val c = Utils.decompose(surface.charAt(0))
      val t1c = Utils.getFirstChar(morphemes.head.word)

      return Utils.isMatch(c, t1c(0))
    }

    true
  }

  private def isHanja(txt: String): Boolean = {
    val chars = txt.toCharArray

    chars.foreach(c => {
      val charType = CharTypeChecker.charType(c)
      if(charType != CharType.HANJA){
        return false
      }
    })

    true
  }

  private def isDigit(txt: String): Boolean = {
    val chars = txt.toCharArray

    chars.foreach(c => {
      val charType = CharTypeChecker.charType(c)
      if(charType != CharType.DIGIT){
        return false
      }
    })

    true
  }

  private def isAlpha(txt: String): Boolean = {
    val chars = txt.toCharArray

    chars.foreach(c => {
      val charType = CharTypeChecker.charType(c)
      if(charType != CharType.ALPHA){
        return false
      }
    })

    true
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

  private def isRemoveTag(tag: String): Boolean = {
    tag.startsWith("S") || tag == "NA"
  }

  private def toCost(p: Float) = {
    val w = WEIGHT
    val score = Math.log(p)
    (-w * score).toShort
  }
}
