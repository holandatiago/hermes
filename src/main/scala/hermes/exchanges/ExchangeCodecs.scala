package hermes.exchanges

import java.sql.Timestamp

import hermes.exchanges.ExchangeModels._
import hermes.exchanges.utils.OrderSide
import spray.json._

trait ExchangeCodecs extends DefaultJsonProtocol {
  implicit def readFunc2JsonFormat[T](implicit f: JsValue => T) = new RootJsonFormat[T] {
    def write(obj: T) = JsNull
    def read(json: JsValue) = f(json)
  }

  implicit object TimestampJsonFormat extends JsonFormat[Timestamp] {
    def write(x: Timestamp) = {
      require(x ne null)
      JsString(x.toString)
    }

    def read(value: JsValue) = value match {
      case JsNumber(x) => new Timestamp(x.longValue)
      case JsString(x) => Timestamp.valueOf(x)
      case x => deserializationError("Expected Timestamp as JsString, but got " + x)
    }
  }

  implicit object OrderSideJsonFormat extends JsonFormat[OrderSide] {
    def write(x: OrderSide) = {
      require(x ne null)
      JsString(x.toString)
    }

    def read(value: JsValue) = value match {
      case JsString(x) => OrderSide(x)
      case x => deserializationError("Expected OrderSide as JsString, but got " + x)
    }
  }

  implicit val emptyCodec = jsonFormat0[Option[Nothing]](() => None)

  def marketCodec(json: JsValue): Market

  def tickerCodec(json: JsValue): Ticker

  def orderPageCodec(json: JsValue): OrderPage

  def orderBookCodec(json: JsValue): OrderBook

  def tradeCodec(json: JsValue): Trade

  def openOrderCodec(json: JsValue): OpenOrder

  def balanceCodec(json: JsValue): Balance
}
