package hermes

import hermes.bots.Bot
import hermes.config.Config
import hermes.exchanges.ExchangeClient

object Main extends App {
  val config = Config(args)
  val bot = Bot(config.strategy, ExchangeClient(config.account))

  def start(): Unit = bot.start()
  def stop(): Unit = bot.stop()
}
