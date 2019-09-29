package hermes.exchanges

import java.sql.Timestamp

object ExchangeModels {
  case class Market(
      name: String,
      baseCurrency: String,
      quoteCurrency: String,
      minPrice: BigDecimal,
      tickPrice: BigDecimal,
      minBaseVolume: BigDecimal,
      tickBaseVolume: BigDecimal,
      minQuoteVolume: BigDecimal,
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

  sealed trait OrderStatus

  object OrderStatus {
    case object New extends OrderStatus
    case object Rola extends OrderStatus

    def apply(status: String): OrderStatus = {
      List(New, Rola).find(_.toString.toLowerCase == status.toLowerCase)
          .getOrElse(throw new NoSuchElementException(s"OrderStatus $status"))
    }
  }
}
