package com.learn.spark.mllib

import scala.util.control.Breaks

/**
 * Created by xiaojie on 17/7/21.
 * 随机梯度下降算法
 */
object LearnSGD {

    def main(args: Array[String]) {

        /**
         * 训练集
         * 每个样本点有3个分量 (x0,x1,x2)
         **/
        val x: Array[(Double, Double, Double)] = Array((1.0, 0.0, 3.0), (1.0, 1.0, 3.0), (1.0, 2.0, 3.0),
            (1.0, 3.0, 2.0), (1.0, 4.0, 4.0))

        //y[i] 样本点对应的输出
        val y: Array[Double] = Array(95.364, 97.217205, 75.195834, 60.105519, 49.342380)

        //迭代阀值，当两次迭代损失函数之差小于该阀值时停止迭代
        val epsilon: Double = 0.0001

        //学习率
        val alpha: Double = 0.01
        var diff: Double = 0
        var error1: Double = 0
        var error0: Double = 0
        var cnt = 0
        val m = x.length

        //初始化参数
        var theta0: Double = 0
        var theta1: Double = 0
        var theta2: Double = 0

        //可中断的循环
        val loop = new Breaks
        loop.breakable {

            while(true){
                cnt += 1
                //参数迭代计算
                for(i <- 0 until m){
                    //拟合函数为 y = theta0 * x[0] + theta1 * x[1] +theta2 * x[2]
                    //计算残差
                    diff = (theta0 + theta1 * x(i)._2 + theta2 * x(i)._3) - y(i)

                    //梯度 = diff[0] * x[i][j]
                    theta0 -= alpha * diff * x(i)._1
                    theta1 -= alpha * diff * x(i)._2
                    theta2 -= alpha * diff * x(i)._3

                }

                //计算损失函数
                error1 = 0
                for(i <- 0 until m) {

                    val lostFuc = (y(i) - (theta0 + theta1 * x(i)._2 + theta2 * x(i)._3))
                    error1 += (Math.pow(lostFuc, 2) / 2)

                    if(Math.abs(error1 - error0) < epsilon)
                        loop.break
                    else
                        error0 = error1
                }

                println(s"theta0 : $theta0, theta1 : $theta1, theta2 : $theta2, error1 : $error1")
            }
        }

        println(s"Done: theta0 : $theta0, theta1 : $theta1, theta2 : $theta2")
        println(s"迭代次数: $cnt")

        //对x0中的值进行预测
        val y0 = theta0 * x(0)._1 + theta1 * x(0)._2 + theta2 * x(0)._3
        println(s"y0=${y(0)} predict value=$y0")

    }

}
