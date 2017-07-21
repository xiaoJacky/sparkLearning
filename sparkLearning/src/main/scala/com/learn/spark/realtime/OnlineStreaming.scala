package com.learn.spark.realtime

import com.learn.spark.utils.JsonParser
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by xiaojie on 17/7/6.
 * 用于验证stateSnapshots副本数据是否是执行过程中的所有的rdd累加值
 * 经验证确实是累加值 慎用
 */
object OnlineStreaming {

    @SerialVersionUID(4179549560142368882L)
    class Book(val author: String, val content: String, val id: String,
               val time: Long, val title: String) extends Serializable {
        override def hashCode(): Int = {
            id.hashCode
        }

        override def equals(obj: scala.Any): Boolean = {
            if (obj.getClass != this.getClass) {
                false
            } else {
                val o = obj.asInstanceOf[Book]
                if (o.id.equals(this.id)) true else false
            }
        }
    }

    //隐式转换
    implicit class JsonToBean(json: String) {
        def stringToBook: Book = {
            implicit val mf = manifest[Book]
            JsonParser.parseJsonToBean[Book](json)
        }
    }

    implicit class BeanToString[A <: AnyRef](a: A) {
        def toJsonString: String = {
            JsonParser.parseBeanToString(a)
        }
    }

    val mappingFunc = (word: Book, one: Option[List[Book]], state: State[Book]) => {

        val current = one.getOrElse(List[Book]())

        if (current.nonEmpty) {
            val book = current.head
            state.update(book)
        }

        (word, state)
    }

    /**
     * 初始化StreamingContext
     *
     * @param master  spark执行模式
     * @param appName spark appName
     * @param batchDuration 窗口时间
     * @param checkpointDirectory
     */
    def createContext(master: String, appName: String, batchDuration: Duration, checkpointDirectory: String) = {
        val conf = new SparkConf().setAppName(appName).setMaster(master)
                .set("spark.driver.host", "localhost") //本地调试配置
        val ssc = new StreamingContext(conf, batchDuration)
        ssc.checkpoint(checkpointDirectory)

        val mapStateStream = ssc.socketTextStream("localhost", 9090).map(i => {
            Future{
                for(i <- (1 to 10)) {
                    Thread.sleep(8000)
                    println("process in the future")
                }
            }
            (i.stringToBook, List(i.stringToBook))}
        )
                .reduceByKey(_ ++ _).mapWithState(StateSpec.function(mappingFunc)
                .initialState(ssc.sparkContext.emptyRDD[(Book, Book)]))

        mapStateStream.print(10)

        //stateSnapshots副作用 会有副本, 所有的rdd进行累加并且不清除 慎用
        mapStateStream.stateSnapshots().foreachRDD {
            rdd =>
                println("count is: " + rdd.count)
                rdd.foreachPartition(partition => {
                    partition.foreach(i => println("partition foreach data: " + i._1.toJsonString))
                })
        }

        ssc
    }

    def main(args: Array[String]) {
        val ssc = StreamingContext.getOrCreate(Config.checkpointDirectory, () => {
            createContext(Config.master, Config.appName, Config.batchDuration, Config.checkpointDirectory)
        })
        ssc.start() // Start the computation
        ssc.awaitTermination() // Wait for the computation to terminate

    }

}


@SerialVersionUID(4179549560142368883L)
object Config extends Serializable {
    val checkpointDirectory = "/Users/xiaojie/checkpointDirectory"
    val master = "local[4]"
    val appName = "socketStreaming"
    val batchDuration = Seconds(1)
}
