package com.cryptotrader.bots

import com.cryptotrader.exchanges.bittrex.v1.BittrexClient
import com.cryptotrader.exchanges.bittrex.v1.models._

class SpreadBot(exchange: BittrexClient) extends Runnable {
  def run(): Unit = {
    while (true) {
      searchForBestMarket
          .foreach(tradeOnMarket)
    }
  }

  def searchForBestMarket: Option[Market] = {
    val balances = exchange.getBalances.map(bal => (bal.Currency, bal)).toMap
    val markets = exchange.getMarkets.map(mkt => (mkt.MarketName, mkt)).toMap

    exchange.getTickers
        .filter(_.Volume > 5)
        .sortBy(tkr => tkr.Ask / tkr.Bid)
        .reverse
        .map(_.MarketName)
        .map(markets)
        .find(mkt => balances(mkt.BaseCurrency).Available > mkt.MinTradeSize)
  }

  def tradeOnMarket(market: Market): Unit = {
    val orderBook = exchange.getOrderBook(market.MarketName)
    val trades = exchange.getTrades(market.MarketName)

    //balances(market.targetCurrency).available > 0
  }
}
