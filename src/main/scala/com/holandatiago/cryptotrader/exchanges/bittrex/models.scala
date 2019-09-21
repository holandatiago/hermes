package com.holandatiago.cryptotrader.exchanges.bittrex

import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

object models {
  implicit val market = jsonFormat11(Market)
  implicit def bittrexResponse[T: JsonFormat] = jsonFormat3(BittrexResponse[T])

  final case class BittrexResponse[T](
      success: Boolean,
      message: String,
      result: Option[T])

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

  final case class Order(
      quantity: BigDecimal,
      rate: BigDecimal)

  final case class OrderBook(
      buyOrders: List[Order],
      sellOrders: List[Order])

  final case class Trade(
      id: BigDecimal,
      timestamp: String,
      quantity: BigDecimal,
      price: BigDecimal,
      total: BigDecimal,
      fillType: String,
      orderType: String)

  final case class OrderUuid(
      value: String)

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

  final case class Balance(
      currency: String,
      balance: BigDecimal,
      available: BigDecimal,
      pending: BigDecimal,
      cryptoAddress: Option[String],
      requested: Option[Boolean],
      uuid: Option[String])

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
}
