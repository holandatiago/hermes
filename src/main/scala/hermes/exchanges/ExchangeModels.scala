package hermes.exchanges

import java.time.Instant

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
      timestamp: Instant)

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
      timestamp: Instant,
      side: OrderSide)

  case class OpenOrder(
      id: String,
      market: String,
      side: OrderSide,
      price: BigDecimal,
      volume: BigDecimal,
      remainingVolume: BigDecimal,
      timestamp: Instant)

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

    val values = List(Buy, Sell)
    def apply(side: String): OrderSide = {
      values.find(_.toString.toLowerCase == side.toLowerCase)
          .getOrElse(throw new NoSuchElementException(s"OrderSide $side"))
    }
  }
}
