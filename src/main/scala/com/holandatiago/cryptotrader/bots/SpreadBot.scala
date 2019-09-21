package com.holandatiago.cryptotrader.bots

import com.holandatiago.cryptotrader.exchanges.Exchange
import com.holandatiago.cryptotrader.models.Market

class SpreadBot(exchange: Exchange) extends Runnable {
  def run(): Unit = {
    while (true) {
      val bestMarket = searchForBestMarket
      tradeOnMarket(bestMarket)
    }
  }

  def searchForBestMarket: Market = {
    val balances = exchange.getBalances.map(bal => (bal.currency, bal)).toMap
    var bestMarketOption: Option[Market] = None

    while (bestMarketOption.isEmpty) {
      val summaries = exchange.getMarketSummaries
      bestMarketOption = summaries
          .filter(_.market.isActive)
          .filter(_.volume > 5)
          .sortBy(_.spread).reverse
          .map(_.market)
          .find { market =>
            (balances(market.baseCurrency).available > 0) ||
                (balances(market.targetCurrency).available > 0)
          }
    }

    bestMarketOption.get
  }

  def tradeOnMarket(market: Market): Unit = {
    /*val orderBook = exchange.getOrderBook(market)

    balances(market.targetCurrency).available > 0*/
  }
}
