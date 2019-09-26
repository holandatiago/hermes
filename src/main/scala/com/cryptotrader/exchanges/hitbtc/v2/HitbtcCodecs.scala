package com.cryptotrader.exchanges.hitbtc.v2

import java.sql.Timestamp

import com.cryptotrader.exchanges.ExchangeCodecs
import com.cryptotrader.exchanges.ExchangeModels._
import com.cryptotrader.exchanges.utils.OrderSide
import spray.json._

object HitbtcCodecs extends ExchangeCodecs {
  implicit def marketCodec(json: JsValue) = Market(
    name = fromField[String](json, "id"),
    baseCurrency = fromField[String](json, "baseCurrency"),
    quoteCurrency = fromField[String](json, "quoteCurrency"),
    minPrice = BigDecimal(0),
    minVolume = BigDecimal(0),
    tickPrice = fromField[BigDecimal](json, "tickSize"),
    tickVolume = fromField[BigDecimal](json, "quantityIncrement"),
    active = fromField[Boolean](json, "IsActive"))

  implicit def tickerCodec(json: JsValue) = Ticker(
    market = fromField[String](json, "symbol"),
    ask = fromField[BigDecimal](json, "ask"),
    bid = fromField[BigDecimal](json, "bid"),
    open = fromField[BigDecimal](json, "open"),
    high = fromField[BigDecimal](json, "high"),
    low = fromField[BigDecimal](json, "low"),
    last = fromField[BigDecimal](json, "last"),
    baseVolume = fromField[BigDecimal](json, "volume"),
    quoteVolume = fromField[BigDecimal](json, "volumeQuote"),
    timestamp = fromField[Timestamp](json, "timestamp"))

  implicit def orderPageCodec(json: JsValue) = OrderPage(
    price = fromField[BigDecimal](json, "price"),
    volume = fromField[BigDecimal](json, "size"))

  implicit def orderBookCodec(json: JsValue) = OrderBook(
    buy = fromField[List[OrderPage]](json, "bid"),
    sell = fromField[List[OrderPage]](json, "ask"))

  implicit def tradeCodec(json: JsValue) = Trade(
    id = fromField[Long](json, "id"),
    price = fromField[BigDecimal](json, "price"),
    volume = fromField[BigDecimal](json, "quantity"),
    timestamp = fromField[Timestamp](json, "timestamp"),
    side = fromField[OrderSide](json, "side"))

  implicit def openOrderCodec(json: JsValue) = OpenOrder(
    id = fromField[String](json, "clientOrderId"),
    market = fromField[String](json, "symbol"),
    status = fromField[String](json, "status"),
    side = fromField[OrderSide](json, "side"),
    price = fromField[BigDecimal](json, "price"),
    volume = fromField[BigDecimal](json, "quantity"),
    remainingVolume = fromField[BigDecimal](json, "cumQuantity"),
    createdAt = fromField[Timestamp](json, "createdAt"),
    updatedAt = fromField[Timestamp](json, "updatedAt"))

  implicit def balanceCodec(json: JsValue) = Balance(
    currency = fromField[String](json, "currency"),
    reserved = fromField[BigDecimal](json, "reserved"),
    available = fromField[BigDecimal](json, "available"))
}
