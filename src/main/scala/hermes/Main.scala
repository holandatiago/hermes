package hermes

import exchanges._

object Main extends App {
  val account = args.head
  val client = ExchangeClient(Account(account))
  bots.SpreadBot(null, client).start()
}
