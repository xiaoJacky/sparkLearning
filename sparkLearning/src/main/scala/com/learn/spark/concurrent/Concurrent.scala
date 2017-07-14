package com.learn.spark.concurrent

import org.apache.commons.lang3.exception.ExceptionUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
 * Created by xiaojie on 17/7/14.
 */
object Concurrent {

    def getListString(): Future[List[String]] = Future {
        List("1", "2", "3")
    }

    def main(args: Array[String]) {

        val f1 = Future {
            "f1"
        }

        val f2 = Future {
            "f2"
        }

        val f3 = Future {
            2342
        }

        val f4 = Future.sequence(Seq(f1, f2, f3))
        val results = Await.result(f4, 4.seconds)

        println(results) // 输出：List(f1, f2, 2342)

        val f5: Future[(String, String, Int)] =
            for {
                r2 <- f2
                r3 <- f3
                r1 <- f1
            } yield (r1.take(1), r2.drop(1), r3 + 1)

        val (f1Str, f2Str, f3Int) = Await.result(f5, 4.seconds)

        println(s"f1: $f1Str, f2: $f2Str, f3: $f3Int") // 输出：f1: f, f2: 2, f3: 2342

        val f6 = Future{
            6 / 0
        }.recover{
            case e: Exception =>
                println(ExceptionUtils.getStackTrace(e))
        }

        Await.result(f6, 1.seconds)


        val f7 = getListString
        f7 foreach(i=>println(i))

        Future {
            println("hello world")
        }
    }

}
