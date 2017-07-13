package com.learn.spark.akka

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
 * Created by xiaojie on 17/7/13.
 */
class Worker_old(val masterHost: String, val masterPort: Int) extends Actor {

    var master: ActorSelection = _

    //建立连接
    override def preStart(): Unit = {
        //在master启动时会打印下面的那个协议, 可以先用这个做一个标志, 连接哪个master
        //继承actor后会有一个context, 可以通过它来连接
        master = context.actorSelection(s"akka.tcp://MasterSystem@$masterHost:$masterPort/user/Master")
        //需要有/user, Master要和master那边创建的名字保持一致
        master ! "connect"
    }

    def receive: Actor.Receive = {
        case "reply" => {
            println("a reply from master")
        }
        case 1000L => {
            master ! 1000L
        }
    }

}

object Worker_old {

    def main(args: Array[String]): Unit = {

        val host = "127.0.0.1"
        val port = 9999

        val masterHost = "127.0.0.1"
        val masterPort = 8888
        // 准备配置
        val configStr =
            s"""
               |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
               |akka.remote.netty.tcp.hostname = "$host"
                                                         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin
        val config = ConfigFactory.parseString(configStr)
        //ActorSystem老大，辅助创建和监控下面的Actor，他是单例的
        val actorSystem = ActorSystem("WorkerSystem", config)
        val actor = actorSystem.actorOf(Props(new Worker_old(masterHost, masterPort)), "Worker")
        actor ! 1000L
        actorSystem.awaitTermination()
    }
}
