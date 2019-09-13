package com.holandatiago.cryptotrader

import pureconfig.generic.auto._
sealed trait MyAdt
case class AdtA(a: String) extends MyAdt
case class AdtB(b: Int) extends MyAdt
final case class Port(value: Int) extends AnyVal
case class MyClass(
    boolean: Boolean,
    port: Port,
    adt: MyAdt,
    list: List[Double],
    map: Map[String, String],
    option: Option[String])

object Main extends App {
  val conf = pureconfig.loadConfig[MyClass]

  val t = new Thread(new bots.SpreadBot)
  t.start()
}
