package com.learn.spark.mllib

import breeze.linalg._
import breeze.numerics.{abs, exp}

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.control.Breaks

/**
 * Created by xiaojie on 17/7/26.
 * 对高斯核模型使用l2约束的最小二乘法分类
 */
object ClassificationConstraint {

    def main(args: Array[String]): Unit = {

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

        //训练模型
        train(gaussKernelModel(xMatrix), yMatrix)
    }

    /**
     * 训练模型
     * @param xMatrix
     * @param yMatrix
     **/
    def train(xMatrix: DenseMatrix[Double], yMatrix: DenseMatrix[Double]): Unit = {

        //x矩阵的转置
        val xMatrixT = xMatrix.t
        val xxMatrixT = inv(xMatrixT * xMatrix).asInstanceOf[DenseMatrix[Double]] //(x矩阵的转置 * x)矩阵的逆
        val xxxMatrixT = (xxMatrixT * xMatrixT).asInstanceOf[DenseMatrix[Double]] //x矩阵的转置 * x)矩阵的逆 * x的转置
        var wMatrix = (xxxMatrixT * yMatrix).asInstanceOf[DenseMatrix[Double]] //x矩阵的转置 * x)矩阵的逆 * x的转置 * y的矩阵
        var wDiagMatrix0 = diag(abs(wMatrix(::, 0))) //加权矩阵对角矩阵

        var count = 0
        val loop = new Breaks
        loop.breakable {
            //迭代训练
            for(h <- 0 until 500000) {

                val xtxMatrix = (xMatrixT * xMatrix).asInstanceOf[DenseMatrix[Double]]
                val addMatrix = xtxMatrix + wDiagMatrix0
                val invMatrix = inv(addMatrix)
                val multMatrix = (invMatrix * xMatrixT).asInstanceOf[DenseMatrix[Double]]
                val lastMatrix = (multMatrix * yMatrix).asInstanceOf[DenseMatrix[Double]]
                val wDiagMatrix1 = diag(abs(lastMatrix(::, 0))) //加权矩阵对角矩阵
                val diffMatrix = lastMatrix - wMatrix
                val diff = norm(diffMatrix.toDenseVector)
                if (diff < 0.001) loop.break else {
                    wDiagMatrix0 = wDiagMatrix1
                    wMatrix = lastMatrix
                }
                count = count + 1
                println(s"第${count}次迭代 diff=$diff")
            }
        }

        println(wMatrix)

        val resultMatrix = (xMatrix * wMatrix).asInstanceOf[DenseMatrix[Double]]
        val m = resultMatrix.rows
        var lost = 0
        var right = 0
        for(i <- 0 until m){
            val y0 = yMatrix.apply(i, 0)
            val y1 = resultMatrix.apply(i, 0)
            if(sign(y1) == y0) right = right + 1 else lost = lost + 1
        }

        println(s"right=$right, lost=$lost")


    }

    /**
     * 高斯核模型
     * @param xMatrix
     **/
    def gaussKernelModel(xMatrix: DenseMatrix[Double]): DenseMatrix[Double] = {
        val hh = 2 * Math.pow(7.9, 2)
        val m = xMatrix.rows
        val denseMatrix = DenseMatrix.rand[Double](m, m)
        for (i <- 0 until m) {
            val buffer = ListBuffer[Double]()
            for (it <- 0 until m) {
                val diffVector = xMatrix(i, ::) - xMatrix(it, ::)
                val number = (diffVector * diffVector.t) / hh
                buffer += number
            }
            denseMatrix(i, ::) := DenseVector.apply[Double](buffer.toArray).t
        }
        exp(denseMatrix)
    }

    /**
     * 阶跃函数
     * @param x
     * */
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
