package hermes.exchanges.binance.v3

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.unmarshalling.Unmarshal
import hermes.exchanges.ExchangeClient
import hermes.exchanges.ExchangeModels._
import hermes.exchanges.binance.v3.BinanceCodecs._
import hermes.exchanges.utils._
import spray.json.RootJsonFormat

class BinanceClient(apiKey: ApiKey) extends ExchangeClient {
  val auth = Auth(apiKey.secret, "HmacSHA256")
  val host = "https://api.binance.com"
  val path = "/api"

  override def buildHttpRequest(method: String, route: List[String], params: Map[String, Any]) = {
    val totalParams = (params + ("timestamp" -> System.currentTimeMillis())).mapValues(_.toString)
    val allParams = totalParams + ("signature" -> auth.generateHmac(Uri.Query(totalParams).toString))
    val uri = Uri(host).withPath(Uri.Path(s"$path/${route.mkString("/")}")).withQuery(Uri.Query(allParams))
    val headers = List(RawHeader("X-MBX-APIKEY", apiKey.public))
    HttpRequest(HttpMethods.getForKey(method).get, uri, headers)
  }

  override def handleHttpResponse[T: RootJsonFormat](response: HttpResponse) = {
    Unmarshal(response).to[T]
  }

  def getMarkets: List[Market] =
    makeRequest[ExchangeInfo]("GET", List("v1", "exchangeInfo")).symbols

  def getTickers: List[Ticker] =
    makeRequest[List[Ticker]]("GET", List("v1", "ticker", "24hr"))

  def getOrderBook(market: String): OrderBook =
    makeRequest[OrderBook]("GET", List("v1", "depth"), Map("symbol" -> market))

  def getLastTrades(market: String): List[Trade] =
    makeRequest[List[Trade]]("GET", List("v1", "trades"), Map("symbol" -> market))

  def sendOrder(market: String, side: OrderSide, price: BigDecimal, volume: BigDecimal): Unit =
    makeRequest[Option[Nothing]]("POST", List("v3", "order"),
      Map("symbol" -> market, "side" -> side, "type" -> "limit", "price" -> price, "quantity" -> volume))

  def cancelOrder(orderId: String): Unit =
    makeRequest[Option[Nothing]]("DELETE", List("v3", "order"), Map("origClientOrderId" -> orderId))

  def getOpenOrders(market: String): List[OpenOrder] =
    makeRequest[List[OpenOrder]]("GET", List("v3", "openOrders"), Map("symbol" -> market))

  def getBalances: List[Balance] =
    makeRequest[AccountInfo]("GET", List("v3", "account")).balances
}
