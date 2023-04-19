package org.olympus.hefesto.exchange

import spray.json._

object Models extends DefaultJsonProtocol {
  case class OptionPrice(
      symbol: String,
      price: Double,
      volatility: Double)
  implicit val optionPriceCodec = lift((json: JsValue) => OptionPrice(
    symbol = fromField[String](json, "symbol"),
    price = fromField[BigDecimal](json, "markPrice").doubleValue(),
    volatility = fromField[BigDecimal](json, "markIV").doubleValue()))

  case class SpotPrice(
      symbol: String,
      price: Double)
  implicit val spotPriceCodec = lift((json: JsValue) => SpotPrice(
    symbol = "",
    price = fromField[BigDecimal](json, "indexPrice").doubleValue()))
}
