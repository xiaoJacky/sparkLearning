package com.learn.spark.mllib

import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by xiaojie on 17/7/1.
 * kmeans 异常检测（欧式距离计算）
 * 数据来源：http://kdd.ics.uci.edu/databases/kddcup99/kddcup99.html
 */
object KMeansExample {

    def main(args: Array[String]) {
        val conf = new SparkConf().setMaster("local[4]").setAppName(s"Kmeans example").set("spark.driver.host", "localhost")
        val sc = new SparkContext(conf)
        val rawData = sc.textFile("/Users/xiaojie/Downloads/kddcup.data")

        val labelsAndData = rawData.map { line =>
            val buffer = line.split(",").toBuffer
            buffer.remove(1, 3)
            val label = buffer.remove(buffer.length - 1)
            val vector = Vectors.dense(buffer.map(_.toDouble).toArray)
            (label, vector)
        }
        val data = labelsAndData.values.cache

        val kmeans = new KMeans()
        val model = kmeans.run(data)
        model.clusterCenters.foreach(println)
    }

}
