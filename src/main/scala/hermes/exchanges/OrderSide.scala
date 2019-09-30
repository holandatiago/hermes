package hermes.exchanges

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
