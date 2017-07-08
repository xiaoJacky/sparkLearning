package com.learn.spark.mllib

import breeze.linalg._

/**
  * Created by xiaojie on 2017/7/8 0008.
  */
object LearnBreeze {

  def main(args: Array[String]): Unit = {
    val x = DenseVector.zeros[Double](5)//全0向量
    println(s"$x\n")

    x(1) = 2
    println(s"$x\n")

    x(3 to 4) := .5
    println(s"$x\n")

    x(0 to 1) := DenseVector(.1, .2)
    println(s"$x\n")

    val o = DenseVector.ones[Double](3)
    println(s"$o\n")//全1向量

    val a = DenseMatrix((1.0, 2.0, 3.0), (4.0, 5.0, 6.0))//2x3矩阵
    val b = DenseMatrix((1.0, 2.0), (3.0, 4.0), (5.0, 6.0))//3x2矩阵

    println("矩阵的乘法")
    println(b * a)

    println("\n矩阵的加法")
    println(b.t + a)

    val c = DenseMatrix.rand(2,3)//2x3的矩阵
    println(s"\nc is:\n$c")

    val d = c.reshape(3,2)
    println(s"\nc is:\n$d")

    //2x3 0矩阵
    println(DenseMatrix.zeros[Double](2, 3))

    //单位矩阵
    println(DenseMatrix.eye[Double](3))

    val e = DenseMatrix.rand(2, 3)
    println(e)
    //矩阵第二行
    println(e(1, ::))
    //矩阵第三列
    println(e(::, 2))

    println(max(e))
    println(inv(e))

  }
}
