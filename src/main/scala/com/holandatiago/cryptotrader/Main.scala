package com.holandatiago.cryptotrader

object Main extends App {
  val exchange = exchanges.Exchange()
  new bots.SpreadBot(exchange)
}
