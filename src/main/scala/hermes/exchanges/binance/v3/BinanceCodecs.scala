package hermes.exchanges.binance.v3

import java.sql.Timestamp

import hermes.exchanges.ExchangeModels._
import spray.json._

object BinanceCodecs extends DefaultJsonProtocol {
  implicit def marketCodec(json: JsValue)(implicit jf: JsonFormat[Filter]) = Market(
    name = fromField[String](json, "symbol"),
    baseCurrency = fromField[String](json, "baseAsset"),
    quoteCurrency = fromField[String](json, "quoteAsset"),
    minPrice = fromField[List[Filter]](json, "filters").find(_.filterType == "PRICE_FILTER").get.minPrice.get,
    tickPrice = fromField[List[Filter]](json, "filters").find(_.filterType == "PRICE_FILTER").get.tickSize.get,
    minBaseVolume = fromField[List[Filter]](json, "filters").find(_.filterType == "LOT_SIZE").get.minQty.get,
    tickBaseVolume = fromField[List[Filter]](json, "filters").find(_.filterType == "LOT_SIZE").get.stepSize.get,
    minQuoteVolume = fromField[List[Filter]](json, "filters").find(_.filterType == "MIN_NOTIONAL").get.minNotional.get,
    active = fromField[String](json, "status") == "TRADING")

  implicit def tickerCodec(json: JsValue) = Ticker(
    market = fromField[String](json, "symbol"),
    ask = fromField[BigDecimal](json, "askPrice"),
    bid = fromField[BigDecimal](json, "bidPrice"),
    open = fromField[BigDecimal](json, "openPrice"),
    high = fromField[BigDecimal](json, "highPrice"),
    low = fromField[BigDecimal](json, "lowPrice"),
    last = fromField[BigDecimal](json, "lastPrice"),
    baseVolume = fromField[BigDecimal](json, "volume"),
    quoteVolume = fromField[BigDecimal](json, "quoteVolume"),
    timestamp = new Timestamp(fromField[Long](json, "closeTime")))

  implicit def orderPageCodec(json: JsValue) = OrderPage(
    price = json.convertTo[List[BigDecimal]].head,
    volume = json.convertTo[List[BigDecimal]].last)

  implicit def orderBookCodec(json: JsValue)(implicit jf: JsonFormat[OrderPage]) = OrderBook(
    buy = fromField[List[OrderPage]](json, "bids"),
    sell = fromField[List[OrderPage]](json, "asks"))

  implicit def tradeCodec(json: JsValue) = Trade(
    id = fromField[Long](json, "id"),
    price = fromField[BigDecimal](json, "price"),
    volume = fromField[BigDecimal](json, "qty"),
    timestamp = new Timestamp(fromField[Long](json, "time")),
    side = if (fromField[Boolean](json, "isBuyerMaker")) OrderSide.Buy else OrderSide.Sell)

  implicit def openOrderCodec(json: JsValue) = OpenOrder(
    id = fromField[String](json, "clientOrderId"),
    market = fromField[String](json, "symbol"),
    status = fromField[String](json, "status"),
    side = OrderSide(fromField[String](json, "side")),
    price = fromField[BigDecimal](json, "price"),
    volume = fromField[BigDecimal](json, "origQty"),
    remainingVolume = fromField[BigDecimal](json, "cummulativeQuoteQty"),
    createdAt = new Timestamp(fromField[Long](json, "time")),
    updatedAt = new Timestamp(fromField[Long](json, "updateTime")))

  implicit def balanceCodec(json: JsValue) = Balance(
    currency = fromField[String](json, "asset"),
    reserved = fromField[BigDecimal](json, "locked"),
    available = fromField[BigDecimal](json, "free"))

  case class ExchangeInfo(symbols: List[Market])
  implicit def exchangeInfoCodec(implicit jf: JsonFormat[Market]) = jsonFormat1(ExchangeInfo)

  case class Filter(
      filterType: String,
      minPrice: Option[BigDecimal],
      tickSize: Option[BigDecimal],
      minQty: Option[BigDecimal],
      stepSize: Option[BigDecimal],
      minNotional: Option[BigDecimal])
  implicit def filterCodec = jsonFormat6(Filter)

  case class AccountInfo(balances: List[Balance])
  implicit def accountInfoCodec(implicit jf: JsonFormat[Balance]) = jsonFormat1(AccountInfo)

  case class Error(code: Int, msg: String)
  implicit def errorCodec = jsonFormat2(Error)
}
