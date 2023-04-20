package org.olympus.hefesto

import org.olympus.hefesto.exchange.Client

object Main extends App {
  Client.fetchMarketPrices.foreach(_.prettyPrinter)
}
