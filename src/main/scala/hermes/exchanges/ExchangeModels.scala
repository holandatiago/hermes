package hermes.exchanges

import java.sql.Timestamp

object ExchangeModels {
  case class Market(
      name: String,
      baseCurrency: String,
      quoteCurrency: String,
      minPrice: BigDecimal,
      minVolume: BigDecimal,
      tickPrice: BigDecimal,
      tickVolume: BigDecimal,
      active: Boolean)

  case class Ticker(
      market: String,
      ask: BigDecimal,
      bid: BigDecimal,
      open: BigDecimal,
      high: BigDecimal,
      low: BigDecimal,
      last: BigDecimal,
      baseVolume: BigDecimal,
      quoteVolume: BigDecimal,
      timestamp: Timestamp)

  case class OrderPage(
      price: BigDecimal,
      volume: BigDecimal)

  case class OrderBook(
      buy: List[OrderPage],
      sell: List[OrderPage])

  case class Trade(
      id: Long,
      price: BigDecimal,
      volume: BigDecimal,
      timestamp: Timestamp,
      side: OrderSide)

  case class OpenOrder(
      id: String,
      market: String,
      status: String,
      side: OrderSide,
      price: BigDecimal,
      volume: BigDecimal,
      remainingVolume: BigDecimal,
      createdAt: Timestamp,
      updatedAt: Timestamp)

  case class Balance(
      currency: String,
      reserved: BigDecimal,
      available: BigDecimal)

  sealed trait OrderSide {
    def opposite: OrderSide
  }

  object OrderSide {
    case object Buy extends OrderSide {
      override def opposite = Sell
    }

    case object Sell extends OrderSide {
      override def opposite = Buy
    }

    def apply(side: String): OrderSide = {
      List(Buy, Sell).find(_.toString.toLowerCase == side.toLowerCase)
          .getOrElse(throw new NoSuchElementException(s"OrderSide $side"))
    }
  }
}
