package com.learn.spark.akka
import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by xiaojie on 17/7/13.
 */
class Master_old extends Actor {

    println("constructor invoked")

    override def preStart(): Unit = {
        println("preStart invoked")
    }

    //用于接收消息,sender就是发送者的代理
    def receive: Actor.Receive = {
        case "connect" => {
            println("a client connected")
            sender ! "reply"
        }

        case "hello" => {
            println("hello")
        }

        case n: Long => {
            squareSum(n)
        }
    }

    //启动spark计算结果
    private def squareSum(n: Long): Long = {
        val conf = new SparkConf().setMaster("local[4]").setAppName("Simple Application")
        val sc = new SparkContext(conf)

        val squareSum = sc.parallelize(1L until n).map { i =>
            i * i
        }.reduce(_ + _)

        println(s"============== The square sum of $n is $squareSum. ==============")

        squareSum
    }

}

object Master_old {

    def main(args: Array[String]): Unit = {
        val host = "127.0.0.1"
        val port = 8888
        // 准备配置
        val configStr =
            s"""
               |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
               |akka.remote.netty.tcp.hostname = "$host"
                                                                      |akka.remote.netty.tcp.port = "$port"
       """.stripMargin
        val config = ConfigFactory.parseString(configStr)
        //ActorSystem老大，辅助创建和监控下面的Actor，他是单例的
        val actorSystem = ActorSystem("MasterSystem", config)

        val master = actorSystem.actorOf(Props(new Master_old), "Master") //Master主构造器会执行
        master ! "hello" //发送信息
        actorSystem.awaitTermination() //让进程等待着, 先别结束
    }
}
