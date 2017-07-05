package com.learn.spark.sql.hbase

/**
 * Created by xiaojie on 17/6/19.
 */

import java.io.Serializable

import com.learn.spark.sql.hbase.Hbase.{HBaseSchemaField, RegisteredSchemaField}
import com.learn.util.SystemConfigTool
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql._
import org.apache.spark.sql.sources.{BaseRelation, TableScan}
import org.apache.spark.sql.types.{DataType, IntegerType, LongType, StringType, StructField, StructType}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._


object Resolver extends Serializable {
  def resolve(hbaseField: HBaseSchemaField, result: Result): Any = {
    val cfColArray = hbaseField.fieldName.split(":", -1)
    val cfName = cfColArray(0)
    val colName = cfColArray(1)
    var fieldRs: Any = null
    //resolve row key otherwise resolve column
    if (cfName == "" && colName == "key") {
      fieldRs = resolveRowKey(result, hbaseField.fieldType)
    } else {
      fieldRs = resolveColumn(result, cfName, colName, hbaseField.fieldType)
    }
    fieldRs
  }

  def resolveRowKey(result: Result, resultType: String): Any = {
    val rowkey = resultType match {
      case "string" =>
        result.getRow.map(_.toChar).mkString
      case "int" =>
        result.getRow.map(_.toChar).mkString.toInt
      case "long" =>
        result.getRow.map(_.toChar).mkString.toLong
    }
    rowkey
  }

  def resolveColumn(result: Result, columnFamily: String, columnName: String, resultType: String): Any = {
    val column = result.containsColumn(columnFamily.getBytes, columnName.getBytes) match {
      case true => {
        resultType match {
          case "string" =>
            Bytes.toString(result.getValue(columnFamily.getBytes, columnName.getBytes))
          //result.getValue(columnFamily.getBytes,columnName.getBytes).map(_.toChar).mkString
          case "int" =>
            Bytes.toInt(result.getValue(columnFamily.getBytes, columnName.getBytes))
          case "long" =>
            Bytes.toLong(result.getValue(columnFamily.getBytes, columnName.getBytes))
          case "float" =>
            Bytes.toFloat(result.getValue(columnFamily.getBytes, columnName.getBytes))
          case "double" =>
            Bytes.toDouble(result.getValue(columnFamily.getBytes, columnName.getBytes))
        }
      }
      case _ => {
        resultType match {
          case "string" =>
            ""
          case "int" =>
            0
          case "long" =>
            0
        }
      }
    }
    column
  }

  /**
   * 获取多版本的数据
   * @param result
   * @param columnFamily
   * @param columnName
   * @param resultType
   **/
  def resolveColumnVersion(result: Result, columnFamily: String, columnName: String, resultType: String): mutable.Buffer[(Long, (String, String))] = {
    val column = result.containsColumn(columnFamily.getBytes, columnName.getBytes) match {
      case true => {
        result.getColumnCells(columnFamily.getBytes, columnName.getBytes).map(i => (i.getTimestamp, (columnName, (Bytes.toString(i.getValue)))))
      }
      case _ => {
        null
      }
    }
    column
  }

}

/**
val hbaseDDL = s"""
      |CREATE TEMPORARY TABLE hbase_people
      |USING com.shengli.spark.hbase
      |OPTIONS (
      |  sparksql_table_schema   '(row_key string, name string, age int, job string)',
      |   hbase_table_name     'people',
      | hbase_table_schema '(:key , profile:name , profile:age , career:job )'
      |)""".stripMargin
  */
case class HBaseRelation(@transient val hbaseProps: Map[String, String])(@transient val sqlContext: SQLContext) extends BaseRelation with Serializable with TableScan {

  val hbaseTableName = hbaseProps.getOrElse("hbase_table_name", sys.error("not valid schema"))
  val hbaseTableSchema = hbaseProps.getOrElse("hbase_table_schema", sys.error("not valid schema"))
  val registerTableSchema = hbaseProps.getOrElse("sparksql_table_schema", sys.error("not valid schema"))
  val rowRange = hbaseProps.getOrElse("row_range", "->")
  val version = hbaseProps.getOrElse("version", "1")
  //get star row and end row
  val range = rowRange.split("->", -1)
  val startRowKey = range(0).trim
  val endRowKey = range(1).trim

  val tempHBaseFields = extractHBaseSchema(hbaseTableSchema)
  //do not use this, a temp field
  val registerTableFields = extractRegisterSchema(registerTableSchema)
  val tempFieldRelation = tableSchemaFieldMapping(tempHBaseFields, registerTableFields)

  val hbaseTableFields = feedTypes(tempFieldRelation)
  val fieldsRelations = tableSchemaFieldMapping(hbaseTableFields, registerTableFields)
  val queryColumns = getQueryTargetCloumns(hbaseTableFields)

  def feedTypes(mapping: Map[HBaseSchemaField, RegisteredSchemaField]): Array[HBaseSchemaField] = {
    val hbaseFields = mapping.map {
      case (k, v) =>
        val field = k.copy(fieldType = v.fieldType)
        field
    }
    hbaseFields.toArray
  }

  def isRowKey(field: HBaseSchemaField): Boolean = {
    val cfColArray = field.fieldName.split(":", -1)
    val cfName = cfColArray(0)
    val colName = cfColArray(1)
    if (cfName == "" && colName == "key") true else false
  }

  //eg: f1:col1  f1:col2  f1:col3  f2:col1
  def getQueryTargetCloumns(hbaseTableFields: Array[HBaseSchemaField]): String = {
    var str = ArrayBuffer[String]()
    hbaseTableFields.foreach { field =>
      if (!isRowKey(field)) {
        str += field.fieldName
      }
    }
    str.mkString(" ")
  }

  lazy val schema = {
    val fields = hbaseTableFields.map { field =>
      val name = fieldsRelations.getOrElse(field, sys.error("table schema is not match the definition.")).fieldName
      val relatedType = field.fieldType match {
        case "string" =>
          SchemaType(StringType, nullable = false)
        case "int" =>
          SchemaType(IntegerType, nullable = false)
        case "long" =>
          SchemaType(LongType, nullable = false)
      }
      StructField(name, relatedType.dataType, relatedType.nullable)
    }
    StructType(fields)
  }

  def tableSchemaFieldMapping(externalHBaseTable: Array[HBaseSchemaField], registerTable: Array[RegisteredSchemaField]): Map[HBaseSchemaField, RegisteredSchemaField] = {
    if (externalHBaseTable.length != registerTable.length) sys.error("columns size not match in definition!")
    val rs = externalHBaseTable.zip(registerTable)
    rs.toMap
  }

  /**
   * spark sql schema will be register
   * registerTableSchema   '(rowkey string, value string, column_a string)'
   */
  def extractRegisterSchema(registerTableSchema: String): Array[RegisteredSchemaField] = {
    val fieldsStr = registerTableSchema.trim.drop(1).dropRight(1)
    val fieldsArray = fieldsStr.split(",").map(_.trim)
    fieldsArray.map { fildString =>
      val splitedField = fildString.split("\\s+", -1)
      RegisteredSchemaField(splitedField(0), splitedField(1))
    }
  }

  //externalTableSchema '(:key , f1:col1 )'
  def extractHBaseSchema(externalTableSchema: String): Array[HBaseSchemaField] = {
    val fieldsStr = externalTableSchema.trim.drop(1).dropRight(1)
    val fieldsArray = fieldsStr.split(",").map(_.trim)
    fieldsArray.map(fildString => HBaseSchemaField(fildString, ""))
  }


  // By making this a lazy val we keep the RDD around, amortizing the cost of locating splits.
  lazy val buildScan = {

    val properties = SystemConfigTool.initSystemProperties("hbase.properties")
    val hbaseConf = HBaseConfiguration.create()
    hbaseConf.set("hbase.zookeeper.quorum", properties.getProperty("hbase.zookeeper.quorum"))
    hbaseConf.set("hbase.mapreduce.scan.maxversions", version)
    hbaseConf.set(TableInputFormat.INPUT_TABLE, hbaseTableName)
    hbaseConf.set(TableInputFormat.SCAN_COLUMNS, queryColumns)
    hbaseConf.set(TableInputFormat.SCAN_ROW_START, startRowKey)
    hbaseConf.set(TableInputFormat.SCAN_ROW_STOP, endRowKey)

    val hbaseRdd = sqlContext.sparkContext.newAPIHadoopRDD(
      hbaseConf,
      classOf[org.apache.hadoop.hbase.mapreduce.TableInputFormat],
      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
      classOf[org.apache.hadoop.hbase.client.Result]
    )


    val rs = hbaseRdd.map(tuple => tuple._2).flatMap(result => {

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
      val fieldMap = colMap.map(i=>(i._2, i._3)).toMap//hbase field map


      val key = rowMap._3 match {
        case "string" => Bytes.toString(result.getRow)
        case "int" => Bytes.toInt(result.getRow)
        case "long" => Bytes.toLong(result.getRow)
        case _ => Bytes.toString(result.getRow)
      } //row key生成

      colMap.map(i => {
        Resolver.resolveColumnVersion(result, i._1, i._2, i._3)
      }).filter(null != _).flatMap(i=>i).groupBy(i=>i._1).map(i=>i._2.map(_._2))
              .map(i=>{
        val map = i.toMap
        val values = new ArrayBuffer[Any]
        values += key
        fieldMap.foreach(i=>{
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

    })

//    val rs = hbaseRdd.map(tuple => tuple._2).map(result => {
//      var values = new ArrayBuffer[Any]()
//      hbaseTableFields.foreach { field =>
//        values += Resolver.resolve(field, result)
//      }
//      Row.fromSeq(values.toSeq)
//    })
    rs
  }

  private case class SchemaType(dataType: DataType, nullable: Boolean)

}
