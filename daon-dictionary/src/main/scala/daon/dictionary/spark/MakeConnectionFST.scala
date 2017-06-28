package daon.dictionary.spark

import java.util

import com.google.protobuf.ByteString
import daon.analysis.ko.fst.DaonFSTBuilder
import org.apache.lucene.util.{IntsRef, IntsRefBuilder}
import org.apache.spark.sql._

import scala.collection.mutable.ArrayBuffer

object MakeConnectionFST {

  case class Connection(word_seqs: Array[Int], count: Long)
  case class ConnectionTemp(surface: String, wordSeqs: ArrayBuffer[Int])

  case class Connections(connections: ArrayBuffer[ConnectionTemp] = ArrayBuffer[ConnectionTemp]())

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

    makeFST(spark, rawSentenceDF)

  }

  def makeFST(spark: SparkSession, rawSentenceDF: DataFrame): ByteString = {
    //sentence 별 연결 word_seq's 추출
    //두개 어절 단위로 구성

    val connectionsDF = makeConnections(spark, rawSentenceDF)

    val set = makeIntsRefs(connectionsDF)

    val fst = DaonFSTBuilder.create().build(set)

    val fstByte = DaonFSTBuilder.toByteString(fst)

    println("connections size : " + set.size() + ", ram used : " + fst.ramBytesUsed() + ", byte : " + fstByte.size())

    fstByte
  }

  private def makeIntsRefs(connectionsDF: Dataset[Connection]): util.Set[IntsRef] = {
    val set: util.Set[IntsRef] = new util.TreeSet[IntsRef]

    connectionsDF.collect().foreach(c => {
      val input = new IntsRefBuilder

      c.word_seqs.foreach(seq => {
        input.append(seq)
      })

      if (input.length > 0) set.add(input.get)
    })

    set
  }

  private def makeConnections(spark: SparkSession, rawSentenceDF: DataFrame): Dataset[Connection] = {
    import spark.implicits._

    val connectionsDF = rawSentenceDF.flatMap(row => {
      val sentence = row.getAs[String]("sentence")
      val eojeols = row.getAs[Seq[Row]]("eojeols")

      //      println(sentence)

      val results = ArrayBuffer[Array[Int]]()

      val connections = Connections()

      eojeols.indices.foreach(e => {
        val eojeol = eojeols(e)
        val surface = eojeol.getAs[String]("surface")
        val morphemes = eojeol.getAs[Seq[Row]]("morphemes")

        //        println(surface)

        val words = ArrayBuffer[Int]()

        morphemes.indices.foreach(m => {
          val morpheme = morphemes(m)
          val seq = morpheme.getAs[Long]("seq").toInt
          val w = morpheme.getAs[String]("word")
          val tag = morpheme.getAs[String]("tag")

          words += seq
        })

        if (words.nonEmpty) {
          connections.connections += ConnectionTemp(surface, words)
        }
      })


      val connectionTemps = connections.connections

      //어절 수
      val len = connectionTemps.length

      connectionTemps.indices.foreach(i => {
        val nextIdx = i + 1

        val wordSeqs = ArrayBuffer[Int]()

        val curConn = connectionTemps(i)

        if (nextIdx < len) {
          val nexConn = connectionTemps(nextIdx)

          //            println("cur : " + curWords.surface + ", " + curWords.wordSeqs)
          //            println("next : " + nextWords.surface + ", " + nextWords.wordSeqs)

          wordSeqs ++= curConn.wordSeqs ++ nexConn.wordSeqs
        }else{
          wordSeqs ++= curConn.wordSeqs
        }

        results += wordSeqs.toArray
      })

      //      wordSeqTemp.words.foreach(println)

      results
    }).as[Array[Int]]

    val df = connectionsDF.withColumnRenamed("value", "word_seqs")

//    df.printSchema
//    df.show(10, truncate = false)

    val dfg = df.groupBy("word_seqs").count().as[Connection]

    dfg
  }

}
