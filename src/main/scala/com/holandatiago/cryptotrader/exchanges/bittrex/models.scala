package com.holandatiago.cryptotrader.exchanges.bittrex

import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

object models {
  final case class BittrexResponse[T](
      success: Boolean,
      message: String,
      result: Option[T])
  implicit def bittrexResponse[T: JsonFormat] = jsonFormat3(BittrexResponse[T])

  final case class Market(
      marketCurrency: String,
      baseCurrency: String,
      marketCurrencyLong: String,
      baseCurrencyLong: String,
      minTradeSize: BigDecimal,
      marketName: String,
      isActive: Boolean,
      created: String,
      notice: Option[String],
      isSponsored: Option[Boolean],
      logoUrl: Option[String])
  implicit val market = jsonFormat11(Market)

  final case class MarketSummary(
      marketName: String,
      high: BigDecimal,
      low: BigDecimal,
      volume: BigDecimal,
      last: BigDecimal,
      baseVolume: BigDecimal,
      timeStamp: String,
      bid: BigDecimal,
      ask: BigDecimal,
      openBuyOrders: BigDecimal,
      openSellOrders: BigDecimal,
      prevDay: BigDecimal,
      created: String,
      displayMarketName: Option[String])
  implicit val marketSummary = jsonFormat14(MarketSummary)

  final case class Order(
      quantity: BigDecimal,
      rate: BigDecimal)
  implicit val order = jsonFormat2(Order)

  final case class OrderBook(
      buyOrders: List[Order],
      sellOrders: List[Order])
  implicit val orderBook = jsonFormat2(OrderBook)

  final case class Trade(
      id: BigDecimal,
      timestamp: String,
      quantity: BigDecimal,
      price: BigDecimal,
      total: BigDecimal,
      fillType: String,
      orderType: String)
  implicit val trade = jsonFormat7(Trade)

  final case class Uuid(
      uuid: String)
  implicit val uuid = jsonFormat1(Uuid)

  final case class OpenOrder(
      uuid: Option[String],
      orderUuid: String,
      exchange: String,
      orderType: String,
      quantity: BigDecimal,
      quantityRemaining: BigDecimal,
      limit: BigDecimal,
      commissionPaid: BigDecimal,
      price: BigDecimal,
      pricePerUnit: Option[BigDecimal],
      opened: String,
      closed: Option[BigDecimal],
      cancelInitiated: Boolean,
      immediateOrCancel: Boolean,
      isConditional: Boolean,
      condition: Option[String],
      conditionTarget: Option[String])
  implicit val openOrder = jsonFormat17(OpenOrder)

  final case class Balance(
      currency: String,
      balance: BigDecimal,
      available: BigDecimal,
      pending: BigDecimal,
      cryptoAddress: Option[String],
      requested: Option[Boolean],
      uuid: Option[String])
  implicit val balance = jsonFormat7(Balance)

  final case class ClosedOrder(
      accountId: Option[String],
      orderUuid: String,
      exchange: String,
      orderType: String,
      quantity: BigDecimal,
      quantityRemaining: BigDecimal,
      limit: BigDecimal,
      reserved: BigDecimal,
      reserveRemaining: BigDecimal,
      commissionReserved: BigDecimal,
      commissionReserveRemaining: BigDecimal,
      commissionPaid: BigDecimal,
      price: BigDecimal,
      pricePerUnit: Option[BigDecimal],
      opened: String,
      closed: Option[String],
      isOpen: Boolean,
      sentinel: String,
      cancelInitiated: Boolean,
      immediateOrCancel: Boolean,
      isConditional: Boolean,
      condition: String)
  implicit val closedOrder = jsonFormat22(ClosedOrder)
}
