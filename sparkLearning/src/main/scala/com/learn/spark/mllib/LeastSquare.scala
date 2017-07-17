package com.learn.spark.mllib

import breeze.linalg._


/**
 * Created by xiaojie on 17/7/17.
 * 最小二乘法 breeze实现
 * 参考博客：
 * http://blog.csdn.net/wangyangzhizhou/article/details/60133958
 */
object LeastSquare {

    def main(args: Array[String]) {

        val x = Array[Double](1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val y = Array[Double](10, 11.5, 12, 13, 14.5, 15.5, 16.8, 17.3, 18, 18.7)

        //构造X矩阵
        val xMatrix = DenseMatrix.ones[Double](10, 2)
        xMatrix(::, 0) := DenseVector.apply[Double](x)

        //构造y矩阵
        val yMatrix = DenseMatrix.apply(y).t

        //x矩阵的转置
        val xMatrixT = xMatrix.t
        val xxMatrixT = inv(xMatrixT * xMatrix).asInstanceOf[DenseMatrix[Double]]//(x矩阵的转置 * x)矩阵的逆
        val xxxMatrixT = (xxMatrixT * xMatrixT).asInstanceOf[DenseMatrix[Double]]//x矩阵的转置 * x)矩阵的逆 * x的转置
        val wMatrix = (xxxMatrixT * yMatrix).asInstanceOf[DenseMatrix[Double]]//x矩阵的转置 * x)矩阵的逆 * x的转置 * y的矩阵
        val a = wMatrix.apply(0, 0)
        val b = wMatrix.apply(1, 0)
        println(s"函数: y = ${a}x + ${b}")

    }

}
