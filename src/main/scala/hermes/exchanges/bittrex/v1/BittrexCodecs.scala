package hermes.exchanges.bittrex.v1

import java.sql.Timestamp

import hermes.exchanges.ExchangeModels._
import spray.json._

object BittrexCodecs extends DefaultJsonProtocol {
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
    timestamp = Timestamp.valueOf(fromField[String](json, "TimeStamp")))

  implicit def orderPageCodec(json: JsValue) = OrderPage(
    price = fromField[BigDecimal](json, "Rate"),
    volume = fromField[BigDecimal](json, "Quantity"))

  implicit def orderBookCodec(json: JsValue)(implicit jf: JsonFormat[OrderPage]) = OrderBook(
    buy = fromField[List[OrderPage]](json, "buy"),
    sell = fromField[List[OrderPage]](json, "sell"))

  implicit def tradeCodec(json: JsValue) = Trade(
    id = fromField[Long](json, "Id"),
    price = fromField[BigDecimal](json, "Price"),
    volume = fromField[BigDecimal](json, "Quantity"),
    timestamp = Timestamp.valueOf(fromField[String](json, "TimeStamp")),
    side = OrderSide(fromField[String](json, "OrderType")))

  implicit def openOrderCodec(json: JsValue) = OpenOrder(
    id = fromField[String](json, "Id"),
    market = fromField[String](json, "Exchange"),
    status = fromField[String](json, "CancelInitiated"),
    side = OrderSide(fromField[String](json, "OrderType")),
    price = fromField[BigDecimal](json, "Price"),
    volume = fromField[BigDecimal](json, "Quantity"),
    remainingVolume = fromField[BigDecimal](json, "QuantityRemaining"),
    createdAt = Timestamp.valueOf(fromField[String](json, "Opened")),
    updatedAt = Timestamp.valueOf(fromField[String](json, "Opened")))

  implicit def balanceCodec(json: JsValue) = Balance(
    currency = fromField[String](json, "Currency"),
    reserved = fromField[BigDecimal](json, "Pending"),
    available = fromField[BigDecimal](json, "Available"))

  case class Response[T](success: Boolean, message: String, result: Option[T])
  implicit def responseCodec[T](implicit jf: JsonFormat[T]) = jsonFormat3(Response[T])
}
