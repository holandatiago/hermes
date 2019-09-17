package com.holandatiago.cryptotrader.exchanges

import com.holandatiago.cryptotrader.models._

object Exchange {
  def apply(name: String): Exchange = name match {
    case "binance" => new Binance()
  }
}

trait Exchange {
  def key
  def timeout
  def getMarkets
  def fee
  def minTradingVolume
  def makeOrder
  def checkOrderStatus
  def checkOpenOrders
  def cancelOrder

  def getBalances: List[Balance]
  def getMarketSummaries: List[MarketSummary]
  def getOrderBook(market: Market): OrderBook
  def getLastTrades(market: Market): List[Trade]
}