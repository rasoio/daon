package daon.dictionary.spark

import java.util

import com.google.protobuf.ByteString
import daon.analysis.ko.fst.DaonFSTBuilder
import daon.analysis.ko.model.ConnectionIntsRef
import org.apache.lucene.util.{IntsRef, IntsRefBuilder}
import org.apache.spark.sql._

import scala.collection.mutable.ArrayBuffer

object MakeConnectionFST {

  case class ConnFSTs(inner: ByteString, outer: ByteString, conn: ByteString)

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

    MakeModel.createSentencesView(spark)

    makeFST(spark, rawSentenceDF)

  }

  def makeFST(spark: SparkSession, rawSentenceDF: DataFrame): ConnFSTs = {

    //sentence 별 연결 word_seq's 추출
    //두개 어절 단위로 구성

    val inner = makeInnerFST(spark)
    val outer = makeOuterFST(spark)
    val conn = makeConnFST(spark, rawSentenceDF)

    println("inner size : " + inner.size() + ", outer size : " + outer.size() + ", conn : " + conn.size())

    ConnFSTs(inner, outer, conn)
  }

  private def makeInnerFST(spark: SparkSession): ByteString = {
    import spark.implicits._

    val innerDF = spark.sql(
      """
        select word_seq, n_inner_seq, count(*) as freq
        from sentence
        where n_inner_seq is not null
        group by word_seq, n_inner_seq
        order by count(*) desc
      """).map(row => {

      val wordSeqs = ArrayBuffer[Int]()
      val count = row.getAs[Long]("freq")
      val seq1 = row.getAs[Long]("word_seq").toInt
      val seq2 = row.getAs[Long]("n_inner_seq").toInt
      wordSeqs += seq1
      wordSeqs += seq2


      Connection(wordSeqs.toArray, count)
    }).as[Connection]



    val set = makeIntsRefs(innerDF)

    val fst = DaonFSTBuilder.create().build(set)

    DaonFSTBuilder.toByteString(fst)
  }


  private def makeOuterFST(spark: SparkSession): ByteString = {
    import spark.implicits._

    val outerDF = spark.sql(
      """
        select word_seq, n_outer_seq, count(*) as freq
        from sentence
        where n_outer_seq is not null
        group by word_seq, n_outer_seq
        order by count(*) desc
      """).map(row => {

      val wordSeqs = ArrayBuffer[Int]()
      val seq1 = row.getAs[Long]("word_seq").toInt
      val seq2 = row.getAs[Long]("n_outer_seq").toInt
      wordSeqs += seq1
      wordSeqs += seq2

      val count = row.getAs[Long]("freq")

      Connection(wordSeqs.toArray, count)
    }).as[Connection]

    val set = makeIntsRefs(outerDF)

    val fst = DaonFSTBuilder.create().build(set)

    DaonFSTBuilder.toByteString(fst)
  }

  private def makeConnFST(spark: SparkSession, rawSentenceDF: DataFrame): ByteString = {
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

        if (nextIdx < len ) {
          val nextConn = connectionTemps(nextIdx)

          //            println("cur : " + curWords.surface + ", " + curWords.wordSeqs)
          //            println("next : " + nextWords.surface + ", " + nextWords.wordSeqs)

//          wordSeqs ++= curConn.wordSeqs ++ nexConn.wordSeqs
          if(nextConn.wordSeqs.nonEmpty) {
            val conn = ArrayBuffer[Int]()
            conn ++= curConn.wordSeqs
            conn +=  nextConn.wordSeqs(0)
            wordSeqs ++= conn

//            println("conn : " + conn + ", cur : " + curConn.wordSeqs + ", next : " + nextConn.wordSeqs)
          }
        }else{
//          wordSeqs ++= curConn.wordSeqs
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

    val set = makeIntsRefs(dfg)

    val fst = DaonFSTBuilder.create().build(set)

    DaonFSTBuilder.toByteString(fst)
  }


  private def makeIntsRefs(connectionsDF: Dataset[Connection]): util.Set[ConnectionIntsRef] = {
    val set: util.Set[ConnectionIntsRef] = new util.TreeSet[ConnectionIntsRef]

    connectionsDF.collect().foreach(c => {
      val input = new IntsRefBuilder

      c.word_seqs.foreach(seq => {
        input.append(seq)
      })

      if (input.length > 0){
        val conn = new ConnectionIntsRef(input.get, c.count)

        println(input.get, c.count)
        set.add(conn)
      }
    })

    set
  }


}
