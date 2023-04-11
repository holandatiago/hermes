package org.olympus.hermes.config

sealed trait Strategy {
  def name: String
}

object Strategy {
  case class Spread(
      name: String,
      account: String,
      rateLimit: Long,
      mainCurrency: String,
      volumeWindow: BigDecimal,
      minimumTicks: Int,
      orderBySpread: Boolean,
      minimumSpread: BigDecimal,
      minimumVolume: BigDecimal,
      maximumAmount: BigDecimal,
      maximumBought: BigDecimal) extends Strategy

  val values: List[Strategy] = Config.value.strategies

  def apply(name: String): Strategy = {
    values.find(_.name.toLowerCase == name.toLowerCase)
        .getOrElse(throw new NoSuchElementException(s"Strategy $name"))
  }
}
