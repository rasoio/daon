package daon.dictionary.spark

import java.io.{ByteArrayOutputStream, FileInputStream}

import com.google.protobuf.CodedInputStream
import daon.analysis.ko.proto.Model
import org.apache.commons.io.IOUtils
import org.apache.spark.sql.SparkSession
import org.elasticsearch.spark._

object SparkJson {


  def main(args: Array[String]) {


    val spark = SparkSession
      .builder()
      .appName("daon dictionary")
      .master("local[*]")
      .config("es.nodes", "localhost")
      .config("es.port", "9200")
      .config("es.index.auto.create", "true")
      .getOrCreate()


    import spark.implicits._

    val captions = spark.read.json("/Users/mac/im2txt/annotations/captions_val2014.json")

    captions.show()


    captions.createOrReplaceTempView("captions")
    captions.cache()

    captions.printSchema()

    val annotations = spark.sql(
      """
        | select annotation.id as anno_id, annotation.image_id, annotation.caption
        | from captions
        | LATERAL VIEW explode(annotations) exploded_annotations as annotation
      """.stripMargin)

    annotations.printSchema()
    annotations.show(10, false)
    annotations.createOrReplaceTempView("annotations")
    annotations.cache()


    val images = spark.sql(
      """
        | select image.id, image.width, image.height, image.license, image.file_name, image.coco_url
        | from captions
        | LATERAL VIEW explode(images) exploded_images as image
      """.stripMargin)

    images.printSchema()
    images.show(10, false)
    images.createOrReplaceTempView("images")
    images.cache()

    val eval = spark.sql(
      """
        | select *
        | from annotations
        | where image_id = 203564
      """.stripMargin)



  }

}
