package org.olympus.hermes.exchanges.hitbtc.v2

import java.time.Instant
import org.olympus.hermes.enums.OrderSide
import org.olympus.hermes.exchanges.ExchangeModels._
import spray.json._

object HitbtcCodecs extends DefaultJsonProtocol {
  implicit def marketCodec = lift((json: JsValue) => Market(
    name = fromField[String](json, "id"),
    baseCurrency = fromField[String](json, "baseCurrency"),
    quoteCurrency = fromField[String](json, "quoteCurrency"),
    minPrice = fromField[BigDecimal](json, "tickSize"),
    tickPrice = fromField[BigDecimal](json, "tickSize"),
    minBaseVolume = fromField[BigDecimal](json, "quantityIncrement"),
    tickBaseVolume = fromField[BigDecimal](json, "quantityIncrement"),
    minQuoteVolume = fromField[BigDecimal](json, "tickSize") * fromField[BigDecimal](json, "quantityIncrement"),
    active = true))

  implicit def tickerCodec = lift((json: JsValue) => Ticker(
    market = fromField[String](json, "symbol"),
    ask = fromField[Option[BigDecimal]](json, "ask").getOrElse(BigDecimal(0)),
    bid = fromField[Option[BigDecimal]](json, "bid").getOrElse(BigDecimal(0)),
    open = fromField[Option[BigDecimal]](json, "open").getOrElse(BigDecimal(0)),
    high = fromField[Option[BigDecimal]](json, "high").getOrElse(BigDecimal(0)),
    low = fromField[Option[BigDecimal]](json, "low").getOrElse(BigDecimal(0)),
    last = fromField[Option[BigDecimal]](json, "last").getOrElse(BigDecimal(0)),
    baseVolume = fromField[Option[BigDecimal]](json, "volume").getOrElse(BigDecimal(0)),
    quoteVolume = fromField[Option[BigDecimal]](json, "volumeQuote").getOrElse(BigDecimal(0)),
    timestamp = Instant.parse(fromField[String](json, "timestamp"))))

  implicit def orderPageCodec = lift((json: JsValue) => OrderPage(
    price = fromField[BigDecimal](json, "price"),
    volume = fromField[BigDecimal](json, "size")))

  implicit def orderBookCodec = lift((json: JsValue) => OrderBook(
    buy = fromField[List[OrderPage]](json, "bid"),
    sell = fromField[List[OrderPage]](json, "ask")))

  implicit def tradeCodec = lift((json: JsValue) => Trade(
    id = fromField[Long](json, "id"),
    price = fromField[BigDecimal](json, "price"),
    volume = fromField[BigDecimal](json, "quantity"),
    timestamp = Instant.parse(fromField[String](json, "timestamp")),
    side = OrderSide(fromField[String](json, "side"))))

  implicit def balanceCodec = lift((json: JsValue) => Balance(
    currency = fromField[String](json, "currency"),
    reserved = fromField[BigDecimal](json, "reserved"),
    available = fromField[BigDecimal](json, "available")))

  implicit def openOrderCodec = lift((json: JsValue) => OpenOrder(
    id = fromField[String](json, "clientOrderId"),
    market = fromField[String](json, "symbol"),
    side = OrderSide(fromField[String](json, "side")),
    price = fromField[BigDecimal](json, "price"),
    volume = fromField[BigDecimal](json, "quantity"),
    remainingVolume = fromField[BigDecimal](json, "quantity") - fromField[BigDecimal](json, "cumQuantity"),
    timestamp = Instant.parse(fromField[String](json, "createdAt"))))

  implicit def openOrderOptionCodec = rootFormat(new OptionFormat[OpenOrder])

  case class Error(code: Int, message: String, description: Option[String])
  implicit def errorCodec = jsonFormat3(Error)

  case class ErrorResponse(error: Error)
  implicit def errorResponseCodec = jsonFormat1(ErrorResponse)
}
