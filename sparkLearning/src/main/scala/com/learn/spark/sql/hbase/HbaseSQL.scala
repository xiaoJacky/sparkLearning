package com.learn.spark.sql.hbase

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by xiaojie on 17/6/19.
 */
object HbaseSQL {

  def main(args: Array[String]) {
    val sparkConf = new SparkConf().setMaster("local[4]").setAppName("hbase sql")
    val sc = new SparkContext(sparkConf)
    val spark = SparkSession.builder().config(sc.getConf).getOrCreate()

    val map = Map(
      "sparksql_table_schema" -> "(key string, event string, CHN string)",
      "hbase_table_name" -> "ASLINK_HU_PSR",
      "hbase_table_schema" -> "(:key, f:event, f:CHN)"
    )

    val hbasetable = spark.read.format("com.learn.spark.sql.hbase").options(map).load()
    hbasetable.printSchema()
    hbasetable.createOrReplaceTempView("ASLINK_HU_PSR")

    val records = spark.sql("SELECT * from ASLINK_HU_PSR limit 10").collect
    records.foreach(println)
  }

}
