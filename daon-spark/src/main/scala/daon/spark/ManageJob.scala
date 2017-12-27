package daon.spark

import org.apache.spark.sql.SparkSession

trait ManageJob {
  def execute(sparkSession: SparkSession)
}
