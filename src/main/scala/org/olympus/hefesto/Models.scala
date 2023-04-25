package org.olympus.hefesto

import java.time._

object Models {
  sealed trait OptionSide
  object OptionSide {
    case object CALL extends OptionSide
    case object PUT extends OptionSide
  }

  case class MarketInfo(
      currentTime: LocalDateTime,
      underlyingInfo: List[UnderlyingAsset],
      optionInfo: List[OptionAsset])

  case class UnderlyingAsset(
      underlying: String,
      baseAsset: String,
      quoteAsset: String,
      spot: Double,
      options: List[OptionAsset])

  case class OptionAsset(
      symbol: String,
      underlying: String,
      term: LocalDate,
      timeToExpiry: Double,
      strike: Double,
      side: OptionSide,
      price: Double,
      volatility: Double)
}
