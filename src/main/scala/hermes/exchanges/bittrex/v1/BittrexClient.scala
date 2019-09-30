package hermes.exchanges.bittrex.v1

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.unmarshalling.Unmarshal
import hermes.exchanges.ExchangeClient._
import hermes.exchanges.ExchangeModels._
import hermes.exchanges.bittrex.v1.BittrexCodecs._
import hermes.exchanges.{ExchangeClient, OrderSide}
import spray.json.RootJsonFormat

object BittrexClient {
  val name = "bittrex"
}

case class BittrexClient(publicKey: String, privateKey: String) extends ExchangeClient {
  protected val auth = Auth(privateKey, "HmacSHA512")
  protected val host = "https://api.bittrex.com"
  protected val path = "/api/v1.1"

  protected def buildHttpRequest(method: String, route: List[String], params: Map[String, Any]) = route.head match {
    case "public" =>
      val allParams = params.mapValues(_.toString)
      val uri = Uri(host).withPath(Uri.Path(s"$path/${route.mkString("/")}")).withQuery(Uri.Query(allParams))
      HttpRequest(HttpMethods.getForKey(method).get, uri, Nil)
    case _ =>
      val apiKeyParams = Map("apiKey" -> publicKey, "nonce" -> System.currentTimeMillis())
      val allParams = (params ++ apiKeyParams).mapValues(_.toString)
      val uri = Uri(host).withPath(Uri.Path(s"$path/${route.mkString("/")}")).withQuery(Uri.Query(allParams))
      val headers = List(RawHeader("apisign", auth.generateHmac(uri.toString)))
      HttpRequest(HttpMethods.getForKey(method).get, uri, headers)
  }

  protected def handleHttpResponse[T: RootJsonFormat](response: HttpResponse) = {
    Unmarshal(response).to[Response[T]].map {
      case Response(true, _, result) => result.getOrElse(None.asInstanceOf[T])
      case Response(false, "ORDER_NOT_OPEN", _) => None.asInstanceOf[T]
      case Response(false, msg, _) => sys.error(msg)
    }
  }

  def getFee: BigDecimal = BigDecimal("0.0025")

  def getMarkets: List[Market] =
    makeRequest[List[Market]]("GET", List("public", "getmarkets"))

  def getTickers: List[Ticker] =
    makeRequest[List[Ticker]]("GET", List("public", "getmarketsummaries"))

  def getOrderBook(market: String): OrderBook =
    makeRequest[OrderBook]("GET", List("public", "getorderbook"), Map("market" -> market, "type" -> "both"))

  def getLastTrades(market: String): List[Trade] =
    makeRequest[List[Trade]]("GET", List("public", "getmarkethistory"), Map("market" -> market)).sortBy(_.id)

  def getBalances: List[Balance] =
    makeRequest[List[Balance]]("GET", List("account", "getbalances"))

  def getOpenOrders(market: String): List[OpenOrder] =
    makeRequest[List[OpenOrder]]("GET", List("market", "getopenorders"), Map("market" -> market))

  def sendOrder(market: String, side: OrderSide, price: BigDecimal, volume: BigDecimal): String =
    makeRequest[Uuid]("GET", List("market", side.toString.toLowerCase + "limit"),
      Map("market" -> market, "timeInForce" -> "GTC", "rate" -> price, "quantity" -> volume)).uuid

  def cancelOrder(orderId: String): Unit =
    makeRequest[Option[Nothing]]("GET", List("market", "cancel"), Map("uuid" -> orderId))
}
