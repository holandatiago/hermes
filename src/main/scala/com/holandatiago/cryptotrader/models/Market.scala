package com.holandatiago.cryptotrader.models

case class Market(
    name: String,
    baseCurrency: String,
    targetCurrency: String,
    minTradeSize: BigDecimal,
    isActive: Boolean)
