package com.learn.spark.types

/**
 * Created by xiaojie on 17/7/6.
 */
object LearnType {

    class Animal {}

    class Bird extends Animal {}

    //协变
    class Covariant[+T](t: T) {}

    //逆变
    class Contravariant[-T](t: T) {
    }

    //在Scala当中所有的类型都继承自Any，而所有的类型都被Nothing继承
    trait S[-A, +B] {
        //协变是指把类型参数替换为其父类的能力，即妥协的变化.逆变是指把类型参数替换为其子类的能力，及逆天的变化
        def something(a: A): B //参数是逆变 返回值是协变
    }

    trait S1[A, B] {
        def A2B(a: A): B
    }

    class S2 extends S1[String, Int] {
        override def A2B(a: String): Int = a.toInt
    }


    def log[T](list: List[T]): Unit = println(list)

    def say(word: Int)(implicit f: (Int) => String): Unit = println(f(word))

    implicit def int2String(i: Int): String = String.valueOf(i) + "<-int2String"

    def main(args: Array[String]) {
        log[String](List("1", "2", "3"))
        val s2 = new S2
        println(s2.A2B("2"))

        val s1 = new S[Any, String] {
            override def something(a: Any): String = "hello " + a
        }

        println(s1.something(1))

        type m = {def say(word: String): Unit}
        def g(word: String, f: m) = f.say(word)
        g("hello", new {
            def say(word: String): Unit = println(word)
        })

        say(2)

        val cov = new Covariant[Bird](new Bird)
        val cov2: Covariant[Animal] = cov //协变

        val c: Contravariant[Animal] = new Contravariant[Animal](new Animal)
        val c2: Contravariant[Bird] = c //逆变

    }
}
