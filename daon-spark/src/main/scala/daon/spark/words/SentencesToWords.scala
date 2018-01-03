package daon.spark.words

import daon.core.config.CharType
import daon.core.data.{Eojeol, Morpheme, Sentence, Word}
import daon.core.util.{CharTypeChecker, Utils}
import daon.spark.{AbstractWriter, ManageJob}
import org.apache.spark.sql._
import org.apache.spark.sql.functions.count

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}

object SentencesToWords extends AbstractWriter with ManageJob {


  def main(args: Array[String]) {

    val spark = getSparkSession()

    execute(spark)
  }

  override def getJobName() = "sentence_to_words"

  override def execute(spark: SparkSession): Unit = {

    val version = CONFIG.getString("index.words.version")
    val scheme = CONFIG.getString("index.words.scheme")
    val typeName = CONFIG.getString("index.words.type")

    val partialWordsIndexName = s"partial_words_$version"
    val singleWordsIndexName = s"single_words_$version"

    createIndex(partialWordsIndexName, scheme)
    createIndex(singleWordsIndexName, scheme)

    val esDF = readESSentences(spark)

    val rawSentenceDF = toRawSentences(spark, esDF)

    //어절 분석 단어
    makePartialWords(spark, rawSentenceDF, partialWordsIndexName, typeName)

    //사전 단어
    makeSingleWords(spark, rawSentenceDF, singleWordsIndexName, typeName)

    esDF.unpersist()
    rawSentenceDF.unpersist()

    addAlias(partialWordsIndexName, "words")
    addAlias(singleWordsIndexName, "words")

  }

  private def makePartialWords(spark: SparkSession, rawSentenceDF: Dataset[Sentence], indexName: String, typeName: String): Unit = {
    import org.apache.spark.sql.functions.count
    import spark.implicits._

    import scala.collection.JavaConversions._

    implicit val WordEncoder: Encoder[Word] = Encoders.bean(classOf[Word])

    val partialWordsTemp = rawSentenceDF.flatMap(row => {
      val eojeols = row.getEojeols

      var words = ArrayBuffer[Word]()

      eojeols.foreach(e=> {
        val surface = e.getSurface
        val morphemes = e.getMorphemes

        words ++= parsePartialWords(surface, morphemes)
      })

      words
    }).as(WordEncoder)

    val df = partialWordsTemp.select($"surface", $"morphemes")
            .groupBy($"surface", $"morphemes")
            .agg(count("*").alias("weight"))
            .orderBy($"surface".asc)

    df.write.format("org.elasticsearch.spark.sql").mode("overwrite").save(s"$indexName/$typeName")

//    rawSentenceDF.unpersist()
  }

  private def makeSingleWords(spark: SparkSession, rawSentenceDF: Dataset[Sentence], indexName: String, typeName: String) = {
    import spark.implicits._

    import scala.collection.JavaConversions._

    implicit val WordEncoder: Encoder[Word] = Encoders.bean(classOf[Word])

    val singleWordsTemp = rawSentenceDF.flatMap(row => {
      val eojeols = row.getEojeols

      var words = ArrayBuffer[Word]()

      eojeols.foreach(e=> {
        val morphemes = e.getMorphemes

        morphemes.filter(singleWordFilter).foreach(m=>{
          words += new Word(m.getWord, ArrayBuffer(m))
        })
      })

      words
    }).as(WordEncoder)

    val df = singleWordsTemp.select($"surface", $"morphemes")
      .groupBy($"surface", $"morphemes")
      .agg(count("*").alias("weight"))
      .orderBy($"surface".asc)

    df.write.format("org.elasticsearch.spark.sql").mode("overwrite").save(s"$indexName/$typeName")
  }

  private def singleWordFilter(m: Morpheme): Boolean ={
    if(m.getWord == null)
      return false

    //외래어, 한자, 숫자, 분석 불가 케이스는 제외
    if(m.getTag == "SL" || m.getTag == "SH" || m.getTag == "SN" || m.getTag == "NA")
      return false

    //조사, 어미 일때 ㄴ, ㄹ, ㅂ 으로 시작하는 경우 제외
    if(m.getTag.startsWith("E") || m.getTag.startsWith("J")){
      if(m.getWord.startsWith("ㄴ") || m.getWord.startsWith("ㄹ") || m.getWord.startsWith("ㅂ")){
        return false
      }
    }

    return true
  }


  private def toRawSentences(spark: SparkSession, esDF: Dataset[Row]): Dataset[Sentence] ={
    implicit val SentenceEncoder: Encoder[Sentence] = Encoders.bean(classOf[Sentence])

    val rawSenetencesDF = esDF.map(row =>{
      val eojeols = row.getAs[Seq[Row]]("eojeols")
      val s = new Sentence()

      eojeols.indices.foreach(e=>{
        val eojeol = eojeols(e)

        val eojeolSeq = eojeol.getAs[Long]("seq").toInt
        val surface = eojeol.getAs[String]("surface")

        val ne = new Eojeol(eojeolSeq, surface)

        val morphemes = eojeol.getAs[Seq[Row]]("morphemes")

        morphemes.indices.foreach(m=>{
          val morpheme = morphemes(m)

          val word = morpheme.getAs[String]("word")
          val tag = morpheme.getAs[String]("tag")

          val nm = new Morpheme(word, tag)
          ne.getMorphemes.add(nm)
        })

        s.getEojeols.add(ne)
      })

      s
    }).as(SentenceEncoder)

    rawSenetencesDF.cache()

    rawSenetencesDF
  }

  def parsePartialWords(surface: String, morphemes: Seq[Morpheme]): ArrayBuffer[Word] = {
    //partial words 추출 결과
    var words = ArrayBuffer[Word]()

    if(surface != null){
      val s = surface.toLowerCase

      //surface 의 특수문자가 morphemes 에 누락된 경우
      val step1Results = step1(s, morphemes)

      step2(words, step1Results)
    }

    words
  }

  /**
    * S 태그 값 제거 결과 리턴
    * @param surface
    * @param morphemes
    * @return
    */
  private def step1(surface: String, morphemes: Seq[Morpheme]): ArrayBuffer[Word] = {
    import scala.collection.JavaConversions._

    var partialResults = ArrayBuffer[Word]()

    var from = 0
    var beginIndex = 0

    morphemes.indices.foreach(m => {
      val morph = morphemes(m)
      val word = morph.getWord.toLowerCase
      val tag = morph.getTag
      val length = word.length

      if(isRemoveTag(tag)){
        val offset = surface.indexOf(word, beginIndex)

        //특수문자가 포함안된 경우.. 처리 곤란
        if(offset == -1){
          return ArrayBuffer[Word]()
        }

        val endIndex = offset
        val until = m

        if(endIndex > beginIndex) {
          val partialSurface = surface.substring(beginIndex, endIndex)
          val partialMorphs = morphemes.slice(from, until).to[ArrayBuffer]

          partialResults += new Word(partialSurface, partialMorphs)
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

      partialResults += new Word(partialSurface, partialMorphs)
    }

    partialResults
  }

  /**
    * 불규칙 사전 구성 결과 리턴
    * @param step1Results
    * @return
    */
  private def step2(words: ArrayBuffer[Word], step1Results: ArrayBuffer[Word]): Unit = {
    import scala.collection.JavaConversions._

    step1Results.foreach(p => {
      val partialResults = ArrayBuffer[ArrayBuffer[Word]]()

      val surface = p.getSurface
      val surfaceLength = surface.length
      val morphemes = p.getMorphemes

      if(surfaceLength <= 2){
        partialResults += ArrayBuffer(new Word(surface, morphemes))

      }else{
        findPartialWords(surface, morphemes, partialResults)
      }

      addWords(words, partialResults)
    })
  }


  def findPartialWords(surface:String, morphemes: Seq[Morpheme], partialResults: ArrayBuffer[ArrayBuffer[Word]]): Unit = {
    import scala.collection.JavaConversions._

    val morphsLength = morphemes.length
    var seqBuffer = ArrayBuffer[Morpheme]()
    var beginIndex = 0

    var m = 0
    while (m < morphsLength) {
      val morph = morphemes(m)
      val word = morph.getWord.toLowerCase
      val tag = morph.getTag

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

        partialResults += ArrayBuffer(new Word(word, morphs))

        //clear buffer
        seqBuffer = ArrayBuffer[Morpheme]()
      }
      //불규칙 처리
      else {
        val irrMorphs = findIrrMorphs(surface, beginIndex, morphemes, m)

        val irrWord = irrMorphs.getSurface
        val irrWordMorphs = irrMorphs.getMorphemes

        length = irrWord.length
        size = irrWordMorphs.size

        if (irrWord.isEmpty) {
          if (partialResults.nonEmpty) {
            partialResults.last.last.getMorphemes ++= irrWordMorphs
          } else {
            seqBuffer ++= irrWordMorphs
          }
        } else {
          partialResults += ArrayBuffer(new Word(irrWord, irrWordMorphs))
        }
      }

      beginIndex += length
      m += size
    }
  }


  private def indexOf(surface: String, fromIndex: Int, morphemes: Seq[Morpheme], m: Int): Int = {

    val morphsLength = morphemes.length
    val word = morphemes(m).getWord
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
        val w = morphemes(i).getWord
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




  // 불규칙일때, 다음 단어까지 포함해서 찾기
  def findIrrMorphs(surface:String, beginIndex: Int, morphemes: Seq[Morpheme], m: Int): Word = {
    import scala.collection.JavaConversions._

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

    new Word(matchSurface, irrMorphs)
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
        val word = w.getWord
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

  private def addWords(words: ArrayBuffer[Word], partials: ArrayBuffer[ArrayBuffer[Word]]): Unit = {
    import scala.collection.JavaConversions._

    if (partials.nonEmpty) {

      val partialResults = partials

      val tmp = ArrayBuffer[Word]()

      val lf = partialResults.scanLeft(tmp)(_ ++ _).drop(1)
      val rf = partialResults.scanRight(tmp)(_ ++ _).drop(1).dropRight(1)

      //앞 어절
      lf.foreach(c => {
        val s = c.map(w => w.getSurface).mkString
        val morphs = c.flatMap(w => w.getMorphemes)
        val morphemes = ArrayBuffer[Morpheme]()
        morphs.indices.foreach(i=>{
          val word = morphs(i).getWord
          val tag = morphs(i).getTag
          morphemes += new Morpheme(i, word, tag)
        })

        if(validatePartialWords(s, morphemes)){
          words += new Word(s, morphemes)
        }
      })

      //뒷 어절
      rf.foreach(c => {
        val s = c.map(w => w.getSurface).mkString
        val morphs = c.flatMap(w => w.getMorphemes)
        val morphemes = ArrayBuffer[Morpheme]()
        morphs.indices.foreach(i=>{
          val word = morphs(i).getWord
          val tag = morphs(i).getTag
          morphemes += new Morpheme(i, word, tag)
        })

        if(validatePartialWords(s, morphemes)){
          words += new Word(s, morphemes)
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
      val t1c = Utils.getFirstChar(morphemes.head.getWord)

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

}
