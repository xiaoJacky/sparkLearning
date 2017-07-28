package com.learn.spark.mllib

import breeze.linalg.{DenseVector, DenseMatrix}

import scala.io.Source

/**
 * Created by xiaojie on 17/7/27.
 * 支持向量机
 * 参考文档：图解机器学习 机器学习实战
 * http://www.cnblogs.com/wsine/p/5180615.html
 */
object LearnSVM {

    def main(args: Array[String]): Unit = {

        val (xMatrix, yMatrix) = loadDataSet
        

    }

    //读取数据
    def loadDataSet(): (DenseMatrix[Double], DenseMatrix[Double]) = {
        val path = Thread.currentThread().getContextClassLoader.getResource("testSet.txt").getPath
        val dataSet = Source.fromFile(path).getLines().map(_.split("\t").map(_.toDouble)).toArray
        val xDataSet = dataSet.map(i => {
            Array(i(0), i(1))
        })

        val yDataSet = dataSet.map(i => {
            Array(i(2))
        })

        val m = dataSet.length
        val n = dataSet.head.length - 1
        val xMatrix = DenseMatrix.rand[Double](m, n)
        val yMatrix = DenseMatrix.rand[Double](m, 1)
        for (i <- 0 until m) {
            xMatrix(i, ::) := DenseVector.apply[Double](xDataSet(i)).t
            yMatrix(i, ::) := DenseVector.apply[Double](yDataSet(i)).t
        }
        (xMatrix, yMatrix)
    }

}
