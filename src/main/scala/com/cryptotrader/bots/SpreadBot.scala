package com.cryptotrader.bots

import com.cryptotrader.exchanges.ExchangeClient
import com.cryptotrader.exchanges.ExchangeModels._

class SpreadBot(exchange: ExchangeClient) extends Runnable {
  def run(): Unit = {
    while (true) {
      searchForBestMarket
          .foreach(tradeOnMarket)
    }
  }

  def searchForBestMarket: Option[Market] = {
    val balances = exchange.getBalances.map(bal => (bal.currency, bal)).toMap
    val markets = exchange.getMarkets.map(mkt => (mkt.name, mkt)).toMap

    exchange.getTickers
        .filter(_.baseVolume > 5)
        .sortBy(tkr => tkr.ask / tkr.bid)
        .reverse
        .map(_.market)
        .map(markets)
        .find(mkt => balances(mkt.baseCurrency).available > mkt.minPrice)
  }

  def tradeOnMarket(market: Market): Unit = {
    val orderBook = exchange.getOrderBook(market.name)
    val trades = exchange.getLastTrades(market.name)

    //balances(market.targetCurrency).available > 0
  }
}
