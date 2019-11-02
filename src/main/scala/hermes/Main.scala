package hermes

import hermes.bots.Bot
import hermes.config.Strategy

import scala.io.StdIn
import scala.util._

object Main extends App {
  val name = args.head
  val bot = Bot(Strategy(name))
  bot.start()
  StdIn.readLine
  bot.stop()
}
