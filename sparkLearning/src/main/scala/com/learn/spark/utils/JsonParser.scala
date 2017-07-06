package com.learn.spark.utils

import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write

class Book(val author: String, val content: String, val id: String, val time: Long, val title: String)

/**
 * Created by xiaojie on 17/7/4.
 */
object JsonParser {

    /**
     * json string to Book
     * @param json
     **/
    def parseJsonToBean[A](json: String)(implicit mf: scala.reflect.Manifest[A]): A = {
        implicit val formats = DefaultFormats
        parse(json).extract[A]
    }

    /**
     * bean to String
     * @param bean
     * */
    def parseBeanToString[T <: AnyRef](bean: T): String = {
        implicit val formats = Serialization.formats(NoTypeHints)
        write(bean)
    }

    def main(args: Array[String]) {

        val c = List(1, 2, 3)
        val d = compact(render(c))
        println(d)

        val json = "{\"author\":\"hll\",\"content\":\"ES即etamsports\",\"id\":\"693\",\"time\":1490165237200,\"title\":\"百度百科\"}"
        implicit val mf = manifest[Book]
        val book = parseJsonToBean[Book](json)
        println(book.content)

        val string = parseBeanToString(book)
        println(string)

    }

}
