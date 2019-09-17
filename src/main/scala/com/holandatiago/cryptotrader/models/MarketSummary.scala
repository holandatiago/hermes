package com.holandatiago.cryptotrader.models

case class MarketSummary(market: Market, bid: BigDecimal, ask: BigDecimal, volume: BigDecimal) {
  def spread: BigDecimal = ask / bid
}
