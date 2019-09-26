package com.cryptotrader.exchanges.bittrex.v1

import java.sql.Timestamp

import com.cryptotrader.exchanges.ExchangeCodecs
import com.cryptotrader.exchanges.ExchangeModels._
import com.cryptotrader.exchanges.utils.OrderSide
import spray.json._

object BittrexCodecs extends ExchangeCodecs {
  implicit def marketCodec(json: JsValue) = Market(
    name = fromField[String](json, "MarketName"),
    baseCurrency = fromField[String](json, "BaseCurrency"),
    quoteCurrency = fromField[String](json, "MarketCurrency"),
    minPrice = fromField[BigDecimal](json, "MinTradeSize"),
    minVolume = fromField[BigDecimal](json, ""),
    tickPrice = fromField[BigDecimal](json, ""),
    tickVolume = fromField[BigDecimal](json, ""),
    active = fromField[Boolean](json, "IsActive"))

  implicit def tickerCodec(json: JsValue) = Ticker(
    market = fromField[String](json, "MarketName"),
    ask = fromField[BigDecimal](json, "Ask"),
    bid = fromField[BigDecimal](json, "Bid"),
    open = fromField[BigDecimal](json, "PrevDay"),
    high = fromField[BigDecimal](json, "High"),
    low = fromField[BigDecimal](json, "Low"),
    last = fromField[BigDecimal](json, "Last"),
    baseVolume = fromField[BigDecimal](json, "BaseVolume"),
    quoteVolume = fromField[BigDecimal](json, "Volume"),
    timestamp = fromField[Timestamp](json, "TimeStamp"))

  implicit def orderPageCodec(json: JsValue) = OrderPage(
    price = fromField[BigDecimal](json, "Rate"),
    volume = fromField[BigDecimal](json, "Quantity"))

  implicit def orderBookCodec(json: JsValue) = OrderBook(
    buy = fromField[List[OrderPage]](json, "buy"),
    sell = fromField[List[OrderPage]](json, "sell"))

  implicit def tradeCodec(json: JsValue) = Trade(
    id = fromField[Long](json, "Id"),
    price = fromField[BigDecimal](json, "Price"),
    volume = fromField[BigDecimal](json, "Quantity"),
    timestamp = fromField[Timestamp](json, "TimeStamp"),
    side = fromField[OrderSide](json, "OrderType"))

  implicit def openOrderCodec(json: JsValue) = OpenOrder(
    id = fromField[String](json, "Id"),
    market = fromField[String](json, "Exchange"),
    status = fromField[String](json, "CancelInitiated"),
    side = fromField[OrderSide](json, "OrderType"),
    price = fromField[BigDecimal](json, "Price"),
    volume = fromField[BigDecimal](json, "Quantity"),
    remainingVolume = fromField[BigDecimal](json, "QuantityRemaining"),
    createdAt = fromField[Timestamp](json, "Opened"),
    updatedAt = fromField[Timestamp](json, "Opened"))

  implicit def balanceCodec(json: JsValue) = Balance(
    currency = fromField[String](json, "Currency"),
    reserved = fromField[BigDecimal](json, "Pending"),
    available = fromField[BigDecimal](json, "Available"))

  case class Response[T](success: Boolean, message: String, result: Option[T])
  implicit def responseCodec[T: JsonFormat] = jsonFormat3(Response[T])
}
