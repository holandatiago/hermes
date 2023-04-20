package org.olympus.hefesto.exchange

import spray.json._

import java.time._

object Models extends DefaultJsonProtocol {
  sealed trait OptionSide
  object OptionSide {
    case object CALL extends OptionSide
    case object PUT extends OptionSide

    def apply(side: String): OptionSide = side match {
      case "CALL" => CALL
      case "PUT" => PUT
    }
  }

  case class UnderlyingAsset(
      underlying: String,
      baseAsset: String,
      quoteAsset: String,
      spot: Double,
      options: List[OptionAsset]) {
    def prettyPrinter = {
      println(s"$underlying\tSPOT: %5.8f".format(spot))
      options.groupBy(_.term).toList.sortBy(_._1.toEpochDay).foreach { case (term, options) =>
        println(s"\t$term")
        options.groupBy(_.strike).toList.sortBy(_._1).foreach { case (strike, options) =>
          val call = options.filter(_.side == OptionSide.CALL).head
          val put = options.filter(_.side == OptionSide.PUT).head
          println(s"\t\tSTRIKE: %5.0f\tPRICES: %8.2f-C %8.2f-P\tVOLS: %6.4f-C %6.4f-P"
            .format(strike, call.price, put.price, call.volatility, put.volatility))
        }
      }
    }
  }

  case class OptionAsset(
      symbol: String,
      underlying: String,
      term: LocalDate,
      strike: Double,
      side: OptionSide,
      price: Double,
      volatility: Double)

  case class MarketInfo(
      underlyingInfo: List[UnderlyingAsset],
      optionInfo: List[OptionAsset])

  val underlyingInfoCodec = listFormat(lift((json: JsValue) => UnderlyingAsset(
    underlying = fromField[String](json, "underlying"),
    baseAsset = fromField[String](json, "baseAsset"),
    quoteAsset = fromField[String](json, "quoteAsset"),
    spot = Double.NaN,
    options = null)))
  implicit val underlyingPriceCodec = lift((json: JsValue) => UnderlyingAsset(
    underlying = null,
    baseAsset = null,
    quoteAsset = null,
    spot = fromField[BigDecimal](json, "indexPrice").doubleValue(),
    options = null))
  val optionInfoCodec = listFormat(lift((json: JsValue) => OptionAsset(
    symbol = fromField[String](json, "symbol"),
    underlying = fromField[String](json, "underlying"),
    term = LocalDate.ofInstant(Instant.ofEpochMilli(fromField[Long](json, "expiryDate")), ZoneOffset.UTC),
    strike = fromField[BigDecimal](json, "strikePrice").doubleValue(),
    side = OptionSide(fromField[String](json, "side")),
    price = Double.NaN,
    volatility = Double.NaN)))
  implicit val optionPriceCodec = lift((json: JsValue) => OptionAsset(
    symbol = fromField[String](json, "symbol"),
    underlying = null,
    term = null,
    strike = Double.NaN,
    side = null,
    price = fromField[BigDecimal](json, "markPrice").doubleValue(),
    volatility = fromField[BigDecimal](json, "markIV").doubleValue()))
  implicit val marketInfoCodec = lift((json: JsValue) => MarketInfo(
    underlyingInfo = fromField[List[UnderlyingAsset]](json, "optionContracts")(underlyingInfoCodec),
    optionInfo = fromField[List[OptionAsset]](json, "optionSymbols")(optionInfoCodec)))
}
