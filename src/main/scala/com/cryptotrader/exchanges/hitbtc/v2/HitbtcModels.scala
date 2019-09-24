package com.cryptotrader.exchanges.hitbtc.v2

import com.cryptotrader.exchanges.ExchangeModels
import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

object HitbtcModels {
  final case class BittrexResponse[T](
      success: Boolean,
      message: String,
      result: Option[T])
  implicit def bittrexResponse[T: JsonFormat] = jsonFormat3(BittrexResponse[T])

  final case class Market(
      MarketCurrency: String,
      BaseCurrency: String,
      MarketCurrencyLong: String,
      BaseCurrencyLong: String,
      MinTradeSize: BigDecimal,
      MarketName: String,
      IsActive: Boolean,
      Created: String,
      Notice: Option[String],
      IsSponsored: Option[Boolean],
      LogoUrl: Option[String])
  implicit val market = jsonFormat11(Market)
  implicit def marketConversion(markets: List[Market]): List[ExchangeModels.Market] = {null}
  implicit def market1Conversion(markets: List[MarketSummary]): List[ExchangeModels.Ticker] = {null}
  implicit def market2Conversion(markets: OrderBook): ExchangeModels.OrderBook = {null}
  implicit def market3Conversion(markets: List[Trade]): List[ExchangeModels.Trade] = {null}
  implicit def market4Conversion(markets: Uuid): Unit = {}
  implicit def market5Conversion(markets: Uuid): Unit = {}
  implicit def market6Conversion(markets: List[Balance]): List[ExchangeModels.Balance] = {null}
  implicit def market7Conversion(markets: List[OpenOrder]): List[ExchangeModels.Order] = {null}

  final case class MarketSummary(
      MarketName: String,
      High: BigDecimal,
      Low: BigDecimal,
      Volume: BigDecimal,
      Last: BigDecimal,
      BaseVolume: BigDecimal,
      TimeStamp: String,
      Bid: BigDecimal,
      Ask: BigDecimal,
      OpenBuyOrders: BigDecimal,
      OpenSellOrders: BigDecimal,
      PrevDay: BigDecimal,
      Created: String,
      DisplayMarketName: Option[String])
  implicit val marketSummary = jsonFormat14(MarketSummary)

  final case class Order(
      Quantity: BigDecimal,
      Rate: BigDecimal)
  implicit val order = jsonFormat2(Order)

  final case class OrderBook(
      buy: List[Order],
      sell: List[Order])
  implicit val orderBook = jsonFormat2(OrderBook)

  final case class Trade(
      Id: BigDecimal,
      TimeStamp: String,
      Quantity: BigDecimal,
      Price: BigDecimal,
      Total: BigDecimal,
      FillType: String,
      OrderType: String)
  implicit val trade = jsonFormat7(Trade)

  final case class Uuid(
      uuid: String)
  implicit val uuid = jsonFormat1(Uuid)

  final case class OpenOrder(
      Uuid: Option[String],
      OrderUuid: String,
      Exchange: String,
      OrderType: String,
      Quantity: BigDecimal,
      QuantityRemaining: BigDecimal,
      Limit: BigDecimal,
      CommissionPaid: BigDecimal,
      Price: BigDecimal,
      PricePerUnit: Option[BigDecimal],
      Opened: String,
      Closed: Option[BigDecimal],
      CancelInitiated: Boolean,
      ImmediateOrCancel: Boolean,
      IsConditional: Boolean,
      Condition: Option[String],
      ConditionTarget: Option[String])
  implicit val openOrder = jsonFormat17(OpenOrder)

  final case class Balance(
      Currency: String,
      Balance: BigDecimal,
      Available: BigDecimal,
      Pending: BigDecimal,
      CryptoAddress: Option[String],
      Requested: Option[Boolean],
      Uuid: Option[String])
  implicit val balance = jsonFormat7(Balance)
}
