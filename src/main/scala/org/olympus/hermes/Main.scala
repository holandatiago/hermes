package org.olympus.hermes

import org.olympus.hermes.bots.Bot
import org.olympus.hermes.bots.spread.SpreadBot
import org.olympus.hermes.config.Strategy

import scala.io.StdIn

object Main extends App {
  val name = args.head
  val bot = Bot(Strategy(name))
  args.tail.headOption.foreach(bot.asInstanceOf[SpreadBot].setMarket)
  println(s"$name bot started. Press Enter to stop.")
  bot.start()
  StdIn.readLine
  bot.stop()
  println(s"$name bot stopped. Shutting down.")
}
