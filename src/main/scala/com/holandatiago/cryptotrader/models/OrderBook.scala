package com.holandatiago.cryptotrader.models

case class OrderBook(market: Market, buy: List[OrderPage], sell: List[OrderPage])
