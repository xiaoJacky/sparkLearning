package com.learn.spark.utils

import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._

class Book(val author: String, val content: String, val id: String, val time: Long, val title: String)

/**
 * Created by xiaojie on 17/7/4.
 */
object JsonParser {

    def main(args: Array[String]) {

        val c = List(1, 2, 3)
        val d = compact(render(c))
        println(d)

        val json = "{\"author\":\"hll\",\"content\":\"ES即etamsports\",\"id\":\"693\",\"time\":1490165237200,\"title\":\"百度百科\"}"
        implicit val formats = DefaultFormats //导入隐式值
        val book: Book = parse(json).extract[Book]
        println(book.content)

    }

}
