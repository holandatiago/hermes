package org.olympus.hermes.exchanges.hitbtc.v2

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.olympus.hermes.enums.OrderSide
import org.olympus.hermes.exchanges.ExchangeClient
import org.olympus.hermes.exchanges.ExchangeModels._
import org.olympus.hermes.exchanges.hitbtc.v2.HitbtcCodecs._
import spray.json.RootJsonFormat

case class HitbtcClient(publicKey: String, privateKey: String, rateLimit: Long = 10L) extends ExchangeClient {
  val host = "https://api.hitbtc.com"
  val path = "/api/2"
  val fee = BigDecimal("0.0007")

  protected val authorization = Authorization(BasicHttpCredentials(publicKey, privateKey))

  protected def createHttpRequest(method: String, route: List[String], params: Map[String, Any]) = route.head match {
    case "public" =>
      val allParams = params.mapValues(_.toString)
      val uri = Uri(host).withPath(Uri.Path(s"$path/${route.mkString("/")}")).withQuery(Uri.Query(allParams))
      HttpRequest(HttpMethods.getForKey(method).get, uri, Nil)
    case _ =>
      val allParams = params.mapValues(_.toString)
      val uri = Uri(host).withPath(Uri.Path(s"$path/${route.mkString("/")}")).withQuery(Uri.Query(allParams))
      val entity = if (method != "POST") HttpEntity.Empty
      else HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, Uri.Query(allParams).toString)
      HttpRequest(HttpMethods.getForKey(method).get, uri, List(authorization), entity)
  }

  protected def handleHttpResponse[T: RootJsonFormat](response: HttpResponse) = response match {
    case HttpResponse(StatusCodes.OK, _, _, _) => Unmarshal(response).to[T]
    case _ => Unmarshal(response).to[ErrorResponse].map {
      case ErrorResponse(Error(20002, "Order not found", _)) => None.asInstanceOf[T]
      case ErrorResponse(Error(_, message, _)) => sys.error(message)
    }
  }

  def getMarkets: List[Market] =
    makeRequest[List[Market]]("GET", List("public", "symbol"))

  def getTickers: List[Ticker] =
    makeRequest[List[Ticker]]("GET", List("public", "ticker"))

  def getOrderBook(market: String): OrderBook =
    makeRequest[OrderBook]("GET", List("public", "orderbook", market), Map("limit" -> 100))

  def getLastTrades(market: String): List[Trade] =
    makeRequest[List[Trade]]("GET", List("public", "trades", market), Map("limit" -> 100, "sort" -> "ASC"))

  def getBalances: List[Balance] =
    makeRequest[List[Balance]]("GET", List("trading", "balance"))

  def getOpenOrders(market: String): List[OpenOrder] =
    makeRequest[List[OpenOrder]]("GET", List("order"), Map("symbol" -> market))

  def sendOrder(market: String, side: OrderSide, price: BigDecimal, volume: BigDecimal): String =
    makeRequest[OpenOrder]("POST", List("order"), Map("symbol" -> market.toLowerCase, "type" -> "limit",
      "timeInForce" -> "GTC", "side" -> side.toString.toLowerCase, "price" -> price, "quantity" -> volume)).id

  def cancelOrder(orderId: String): Boolean =
    makeRequest[Option[OpenOrder]]("DELETE", List("order", orderId)).nonEmpty
}
