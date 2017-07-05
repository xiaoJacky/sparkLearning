package com.learn.spark.sql.hbase

import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.Row

import scala.collection.mutable.ArrayBuffer

/**
 * Created by xiaojie on 17/7/5.
 */
object Transform {

    implicit class TransformResult(val result: Result) {

        def transformResult(hbaseTableFields: Array[Hbase.HBaseSchemaField]): Iterable[Row] = {
            val tableFiled = hbaseTableFields.map(i => {
                val cfColArray = i.fieldName.split(":", -1)
                val cfName = cfColArray(0)
                val colName = cfColArray(1)
                (cfName, colName, i.fieldType)
            })

            val rowMap = tableFiled.filter(i => {
                i._1.equals("") && i._2.equals("key")
            }).apply(0)

            val colMap = tableFiled.filter(i => {
                ! {
                    i._1.equals("") && i._2.equals("key")
                }
            })
            val fieldMap = colMap.map(i => (i._2, i._3)).toMap //hbase field map


            val key = rowMap._3 match {
                case "string" => Bytes.toString(result.getRow)
                case "int" => Bytes.toInt(result.getRow)
                case "long" => Bytes.toLong(result.getRow)
                case _ => Bytes.toString(result.getRow)
            } //row key生成

            colMap.map(i => {
                Resolver.resolveColumnVersion(result, i._1, i._2, i._3)
            }).filter(null != _).flatMap(i => i).groupBy(i => i._1).map(i => i._2.map(_._2))
                    .map(i => {
                val map = i.toMap
                val values = new ArrayBuffer[Any]
                values += key
                fieldMap.foreach(i => {
                    val r = i._2 match {
                        case "string" => map.getOrElse(i._1, "")
                        case "int" => map.getOrElse(i._1, "0").toInt
                        case "long" => map.getOrElse(i._1, "0").toFloat
                        case _ => map.getOrElse(i._1, "")
                    }
                    values += r
                })
                Row.fromSeq(values.toSeq)
            })
        }

    }

}
