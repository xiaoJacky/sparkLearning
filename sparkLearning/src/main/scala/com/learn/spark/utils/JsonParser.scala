package com.learn.spark.utils

import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._

case class Book(val author: String, val content: String, val id: String, val time: Long, val title: String)
case class Person(var map: Map[String, String]){
    def id: String = map.getOrElse("id", "")
}

/**
 * Created by xiaojie on 17/7/4.
 */
object JsonParser {

    /**
     * json string to Book
     * @param json
     **/
    def parseJsonToBean[A](json: String)(implicit mf: scala.reflect.Manifest[A]): A = {
        implicit val formats = Serialization.formats(NoTypeHints)
        read[A](json)
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

        val map = Map("124" -> "456", "333" -> "789")
        val person = Person(map)
        val personStr = parseBeanToString(person)
        println(personStr)
        person.map += ("124" -> "789")
        println(person)

        println(parseJsonToBean[Person](personStr)(manifest[Person]))


    }

}
