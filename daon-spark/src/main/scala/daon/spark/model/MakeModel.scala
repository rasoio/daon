package daon.spark.model

import java.io.{ByteArrayOutputStream, FileOutputStream}
import java.time.LocalDateTime
import java.util
import java.util.Collections

import com.google.protobuf.ByteString
import daon.core.data.{Morpheme, Word}
import daon.core.fst.DaonFSTBuilder
import daon.core.proto
import daon.core.proto.Model
import daon.core.result.KeywordIntsRef
import daon.spark.{AbstractWriter, ManageJob}
import org.apache.commons.lang3.time.StopWatch
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql._
import org.elasticsearch.spark._


object MakeModel extends AbstractWriter with ManageJob {

  case class ModelData(seq: Long, create_date: String, data: Array[Byte], size: Long, dictionary_count: Long, elapsed_time: Long)

  case class TagTran(position: String, tag1: String, var tag2: String = "", cost: Long)

  case class TagTrans(firstTags: util.List[String], middleTags: util.List[String],
                      connectTags: util.List[String], lastTags: util.List[String])


  val WEIGHT = 200

  def main(args: Array[String]) {

    val spark = getSparkSession()

    execute(spark)

  }

  override def getJobName() = "make_model"

  override def execute(spark: SparkSession): Unit = {

    val stopWatch = new StopWatch
    stopWatch.start()

    val wordsDF = readWords(spark)

    val (dictionaryMap, broadWordToIdx) = makeDictionaryMap(spark)

    val wordToIdx = broadWordToIdx.value

    val fstBytes = makeFST(spark, wordsDF, wordToIdx)

    broadWordToIdx.destroy()
    wordsDF.unpersist()

    val tagTrans = readTagTrans(spark)

    val builder = Model.newBuilder

    builder.putAllDictionary(dictionaryMap)
    builder.setWordFst(fstBytes)
    builder.addAllFirstTags(tagTrans.firstTags)
    builder.addAllMiddleTags(tagTrans.middleTags)
    builder.addAllLastTags(tagTrans.lastTags)
    builder.addAllConnectTags(tagTrans.connectTags)

    val model = builder.build

    stopWatch.stop()

    val elapsedTime = stopWatch.getTime

    println(s"total elapsed time : $elapsedTime ms")

    //    writeModelToFile(model)
    writeModelToES(spark, model, elapsedTime)
  }

  def readWords(spark: SparkSession): Dataset[Row] = {
    val wordsDF = spark.read.format("es").load("words")

    wordsDF.createOrReplaceTempView("words")
    wordsDF.cache()

    wordsDF
  }

  def makeDictionaryMap(spark: SparkSession): (util.HashMap[Integer, Model.Keyword], Broadcast[Map[String, Int]]) = {

    implicit val MorphemeEncoder: Encoder[Morpheme] = Encoders.bean(classOf[Morpheme])

    //0~10 은 예약 seq (1 : 숫자, 2: 영문/한자)

    val df = spark.sql(
      """
          SELECT 0 as seq, word, tag
          FROM
          (
            SELECT
              morpheme.word as word,
              morpheme.tag as tag
            FROM words
            LATERAL VIEW explode(morphemes) exploded_morphemes as morpheme
          ) as m
          WHERE word is not null
          GROUP BY word, tag
          ORDER BY word asc
      """).as(MorphemeEncoder)

//    df.cache()
//    df.coalesce(1).write.mode("overwrite").json("/Users/mac/work/corpus/words")

    val dictionaryMap = new util.HashMap[Integer, Model.Keyword]()

    //seq 채번
    var seq = 10
    val words = df.collect().map(w => {
      seq += 1
      w.setSeq(seq)

      val newKeyword = proto.Model.Keyword.newBuilder.setSeq(seq).setWord(w.getWord).setTag(w.getTag).build
      dictionaryMap.put(seq, newKeyword)

      w
    })

//    df.unpersist()

    val wordMap = words.map(w => {

      val seq = w.getSeq
      val word = w.getWord
      val tag = w.getTag

      val key = getKey(word, tag)

      key -> seq
    }).toMap[String, Int]

    val broadcastVar = spark.sparkContext.broadcast(wordMap)

    (dictionaryMap, broadcastVar)
  }


  def getKey(word: String, tag: String): String = {
    word + "||" + tag
  }


  def makeFST(spark: SparkSession, wordDF: Dataset[Row], wordToIdx: Map[String, Int]): ByteString = {


    import org.apache.spark.sql.functions.max

    import scala.collection.JavaConverters._

    implicit val WordEncoder: Encoder[Word] = Encoders.bean(classOf[Word])
    import spark.implicits._

    val df = wordDF.select($"surface", $"morphemes", $"weight")
      .groupBy($"surface", $"morphemes")
      .agg(max("weight").alias("weight"))
      .orderBy($"surface".asc)
      .as(WordEncoder)

//    df.printSchema()

    val maxWeight = df.groupBy().max("weight").collect()(0).getInt(0).toFloat

    val words = df.collect()

    val keywordIntsRefs = new util.ArrayList[KeywordIntsRef]

    words.foreach(w =>{
      val seqs = w.getMorphemes.asScala.map(m=>{
        val word = m.getWord
        val tag = m.getTag

        val key = getKey(word, tag)
        val seq = wordToIdx.getOrElse(key, 0)
        seq
      }).toArray

      val word = w.getSurface
      val p = w.getWeight / maxWeight

      //emission cost 계산 방법 개선 필요
      val cost = toCost(p)

      val keywordIntsRef = new KeywordIntsRef(word, seqs)
      keywordIntsRef.setCost(cost)
      keywordIntsRefs.add(keywordIntsRef)
    })

    Collections.sort(keywordIntsRefs)

    //빌드 fst
    val fst = DaonFSTBuilder.create.buildPairFst(keywordIntsRefs)
    val fstByte = DaonFSTBuilder.toByteString(fst)

//    println(s"keywords size : ${keywordIntsRefs.size()}")

    fstByte
  }

  def readTagTrans(spark: SparkSession): TagTrans = {

    import spark.implicits._
    import scala.collection.JavaConverters._

    val df = spark.read.format("es").load("tag_trans").as[TagTran]

    df.cache()

    val tags = df.collect()

    df.unpersist()

    val first = tags.filter(t => t.position == "first").map(t => {
      s"${t.tag1},${t.cost}"
    })

    val firstList = seqAsJavaListConverter[String](first).asJava

    val middle = tags.filter(t => t.position == "middle").map(t => {
      s"${t.tag1},${t.tag2},${t.cost}"
    })

    val middleList = seqAsJavaListConverter[String](middle).asJava

    val connect = tags.filter(t => t.position == "connect").map(t => {
      s"${t.tag1},${t.tag2},${t.cost}"
    })

    val connectList = seqAsJavaListConverter[String](connect).asJava

    val last = tags.filter(t => t.position == "last").map(t => {
      s"${t.tag1},${t.cost}"
    })

    val lastList = seqAsJavaListConverter[String](last).asJava

    TagTrans(firstList, middleList, connectList, lastList)
  }

  private def toCost(p: Float) = {
    val w = WEIGHT
    val score = Math.log(p)
    (-w * score).toShort
  }




  private def writeModelToFile(model: Model) = {
//    val output = new FileOutputStream("/Users/mac/work/corpus/model/model8.dat")
    val output = new FileOutputStream("/Users/mac/work/daon/daon-core/src/main/resources/daon/core/reader/model.dat")

    model.writeTo(output)

    output.close()
  }

  private def writeModelToES(spark: SparkSession, model: Model, elapsedTime: Long): Unit = {

    val output = new ByteArrayOutputStream()

    model.writeTo(output)

    output.close()

    val data = output.toByteArray

    val dictionaryCount = model.getDictionaryCount

    val modelData = ModelData(System.currentTimeMillis(), LocalDateTime.now.toString, data, data.size, dictionaryCount, elapsedTime)

    val rdd = spark.sparkContext.makeRDD(Seq(modelData))

    rdd.saveToEs("models/model", Map("es.mapping.id" -> "seq"))

  }
}
