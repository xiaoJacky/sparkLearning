package com.learn.spark.sql.hbase

/**
 * Created by xiaojie on 17/6/19.
 */
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.sources.RelationProvider


class DefaultSource extends RelationProvider {
  def createRelation(sqlContext: SQLContext, parameters: Map[String, String]) = {
    HBaseRelation(parameters)(sqlContext)
  }
}

