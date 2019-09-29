package hermes.exchanges.bittrex.v1

import java.time.{LocalDateTime, ZoneOffset}

import hermes.exchanges.ExchangeModels._
import spray.json._

object BittrexCodecs extends DefaultJsonProtocol {
  implicit def marketCodec(json: JsValue) = Market(
    name = fromField[String](json, "MarketName"),
    baseCurrency = fromField[String](json, "MarketCurrency"),
    quoteCurrency = fromField[String](json, "BaseCurrency"),
    minPrice = BigDecimal(0),
    tickPrice = BigDecimal(1, 8),
    minBaseVolume = fromField[BigDecimal](json, "MinTradeSize"),
    tickBaseVolume = BigDecimal(1, 8),
    minQuoteVolume = BigDecimal(5, 4),
    active = fromField[Boolean](json, "IsActive"))

  implicit def tickerCodec(json: JsValue) = Ticker(
    market = fromField[String](json, "MarketName"),
    ask = fromField[BigDecimal](json, "Ask"),
    bid = fromField[BigDecimal](json, "Bid"),
    open = fromField[BigDecimal](json, "PrevDay"),
    high = fromField[BigDecimal](json, "High"),
    low = fromField[BigDecimal](json, "Low"),
    last = fromField[BigDecimal](json, "Last"),
    baseVolume = fromField[BigDecimal](json, "Volume"),
    quoteVolume = fromField[BigDecimal](json, "BaseVolume"),
    timestamp = LocalDateTime.parse(fromField[String](json, "TimeStamp")).toInstant(ZoneOffset.UTC))

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
    timestamp = LocalDateTime.parse(fromField[String](json, "TimeStamp")).toInstant(ZoneOffset.UTC),
    side = OrderSide(fromField[String](json, "OrderType")))

  implicit def openOrderCodec(json: JsValue) = OpenOrder(
    id = fromField[String](json, "OrderUuid"),
    market = fromField[String](json, "Exchange"),
    side = OrderSide(fromField[String](json, "OrderType").substring(6)),
    price = fromField[BigDecimal](json, "Price"),
    volume = fromField[BigDecimal](json, "Quantity"),
    remainingVolume = fromField[BigDecimal](json, "QuantityRemaining"),
    timestamp = LocalDateTime.parse(fromField[String](json, "Opened")).toInstant(ZoneOffset.UTC))

  implicit def balanceCodec(json: JsValue) = Balance(
    currency = fromField[String](json, "Currency"),
    reserved = fromField[BigDecimal](json, "Pending"),
    available = fromField[BigDecimal](json, "Available"))

  case class Response[T](success: Boolean, message: String, result: Option[T])
  implicit def responseCodec[T](implicit jf: JsonFormat[T]) = jsonFormat3(Response[T])
}
