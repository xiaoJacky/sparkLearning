package com.learn.spark.mllib

import breeze.linalg.DenseVector

/**
  * Created by xiaojie on 2017/7/8 0008.
  */
object LearnBreeze {

  def main(args: Array[String]): Unit = {
    val x = DenseVector.zeros[Double](5)
    println(x)
  }

}
