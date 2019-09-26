package hermes.exchanges.hitbtc.v2

import java.sql.Timestamp

import hermes.exchanges.ExchangeModels._
import hermes.exchanges.utils.OrderSide
import spray.json._

object HitbtcCodecs extends DefaultJsonProtocol {
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
    timestamp = Timestamp.valueOf(fromField[String](json, "timestamp")))

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
    timestamp = Timestamp.valueOf(fromField[String](json, "timestamp")),
    side = OrderSide(fromField[String](json, "side")))

  implicit def openOrderCodec(json: JsValue) = OpenOrder(
    id = fromField[String](json, "clientOrderId"),
    market = fromField[String](json, "symbol"),
    status = fromField[String](json, "status"),
    side = OrderSide(fromField[String](json, "side")),
    price = fromField[BigDecimal](json, "price"),
    volume = fromField[BigDecimal](json, "quantity"),
    remainingVolume = fromField[BigDecimal](json, "cumQuantity"),
    createdAt = Timestamp.valueOf(fromField[String](json, "createdAt")),
    updatedAt = Timestamp.valueOf(fromField[String](json, "updatedAt")))

  implicit def balanceCodec(json: JsValue) = Balance(
    currency = fromField[String](json, "currency"),
    reserved = fromField[BigDecimal](json, "reserved"),
    available = fromField[BigDecimal](json, "available"))
}
