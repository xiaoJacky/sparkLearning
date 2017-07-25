package com.learn.spark.mllib

import java.util.Random

import breeze.linalg.{norm, DenseVector, DenseMatrix}
import breeze.numerics._

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.control.Breaks

/**
 * Created by xiaojie on 17/7/25.
 * 基于高斯核函数最小二乘法进行分类
 */
object Classification {

    def main(args: Array[String]): Unit = {

        val path = Thread.currentThread().getContextClassLoader.getResource("testSet.txt").getPath
        val dataSet = Source.fromFile(path).getLines().map(_.split("\t").map(_.toDouble)).toArray
        val xDataSet = dataSet.map(i=>{
            Array(i(0), i(1))
        })

        val yDataSet = dataSet.map(i=>{
            Array(i(2))
        })

        val m = dataSet.length
        val n = dataSet.head.length - 1
        val xMatrix = DenseMatrix.rand[Double](m, n)
        val yMatrix = DenseMatrix.rand[Double](m, 1)
        for(i <- 0 until m){
            xMatrix(i, ::) := DenseVector.apply[Double](xDataSet(i)).t
            yMatrix(i, ::) := DenseVector.apply[Double](yDataSet(i)).t
        }

        val hh = 2 * Math.pow(10, 2)
        val e = 0.01
        var t0 = DenseMatrix.rand[Double](m, 1)
        val threshold = 0.000001
        var count = 0

        //可中断的循环
        val loop = new Breaks
        loop.breakable {
            while (true){

                val r = new Random().nextInt(m)
                val buffer = ListBuffer[Double]()
                for(it <- 0 until m){
                    val diffVector = xMatrix(r, ::) - xMatrix(it, ::)
                    val number = (diffVector * diffVector.t) / hh
                    //                    val number = Math.pow(norm(diffVector.t), 2) / hh
                    buffer += number
                }

                val ki = exp(DenseMatrix.apply(buffer.toArray))
                val diff = (ki * t0).asInstanceOf[DenseMatrix[Double]].apply(0,0) - yMatrix.apply(r, 0)
                val t1 = t0 - (e * ki.t * diff).asInstanceOf[DenseMatrix[Double]]
                val diffT = t1 - t0
                val thresholdDiff = norm(diffT.toDenseVector)
                if(thresholdDiff < threshold) loop.break else t0 = t1
                count = count + 1
            }
        }
        println("迭代次数：" + count)
        println("\nt0矩阵：")
        println(t0)

        var lost = 0
        var right = 0
        for(r <- 0 until m) {
            val buffer = ListBuffer[Double]()
            for (it <- 0 until m) {
                val diffVector = xMatrix(r, ::) - xMatrix(it, ::)
                //            val number = Math.pow(norm(diffVector.t), 2) / hh
                val number = (diffVector * diffVector.t) / hh
                buffer += number
            }

            val ki = exp(DenseMatrix.apply(buffer.toArray))
            val y0 = (ki * t0).asInstanceOf[DenseMatrix[Double]].apply(0, 0)
            if(sign(y0) == yDataSet(r)(0)) right = right + 1 else lost = lost + 1
        }

        println(s"lost=$lost, right=$right")

    }

    def sign(x: Double): Double = {
        if(x > 0 ){
            1.0
        }else if(x == 0){
            -1
        }else {
            0
        }
    }

}
