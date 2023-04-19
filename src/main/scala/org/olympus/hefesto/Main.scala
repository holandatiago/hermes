package org.olympus.hefesto

import org.olympus.hefesto.exchange.Client

object Main extends App {
  println(Client.getOptionPrice("BTC-230505-24000-C"))
  println(Client.getSpotPrice("BTCUSDT"))
}
