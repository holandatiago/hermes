package hermes.exchanges.hitbtc.v2

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.unmarshalling.Unmarshal
import hermes.exchanges.ExchangeClient
import hermes.exchanges.ExchangeClient._
import hermes.exchanges.ExchangeModels._
import hermes.exchanges.hitbtc.v2.HitbtcCodecs._
import spray.json.RootJsonFormat

class HitbtcClient(val apiKey: ApiKey) extends ExchangeClient {
  protected val auth = Authorization(BasicHttpCredentials(apiKey.public, apiKey.secret))
  protected val host = "https://api.hitbtc.com"
  protected val path = "/api/2"

  protected def buildHttpRequest(method: String, route: List[String], params: Map[String, Any]) = {
    route.head match {
      case "public" =>
        val allParams = params.mapValues(_.toString)
        val uri = Uri(host).withPath(Uri.Path(s"$path/${route.mkString("/")}")).withQuery(Uri.Query(allParams))
        HttpRequest(HttpMethods.getForKey(method).get, uri, Nil)
      case _ =>
        val totalParams = params.mapValues(_.toString)
        val allParams = totalParams
        val uri = Uri(host).withPath(Uri.Path(s"$path/${route.mkString("/")}")).withQuery(Uri.Query(allParams))
        val headers = List(auth)
        HttpRequest(HttpMethods.getForKey(method).get, uri, headers)
    }
  }

  protected def handleHttpResponse[T: RootJsonFormat](response: HttpResponse) = {
    response match {
      case HttpResponse(StatusCodes.OK, _, _, _) => Unmarshal(response).to[T]
      case _ => Unmarshal(response).to[ErrorResponse].map(e => sys.error(e.error.message))
    }
  }

  def getMarkets: List[Market] =
    makeRequest[List[Market]]("GET", List("public", "symbol"))

  def getTickers: List[Ticker] =
    makeRequest[List[Ticker]]("GET", List("public", "ticker"))

  def getOrderBook(market: String): OrderBook =
    makeRequest[OrderBook]("GET", List("public", "orderbook", market))

  def getLastTrades(market: String): List[Trade] =
    makeRequest[List[Trade]]("GET", List("public", "trades", market))

  def sendOrder(market: String, side: OrderSide, price: BigDecimal, volume: BigDecimal): Unit =
    makeRequest[Option[Nothing]]("POST", List("order"),
      Map("symbol" -> market, "side" -> side, "price" -> price, "quantity" -> volume))

  def cancelOrder(orderId: String): Unit =
    makeRequest[Option[Nothing]]("DELETE", List("order", orderId))

  def getOpenOrders(market: String): List[OpenOrder] =
    makeRequest[List[OpenOrder]]("GET", List("order"), Map("symbol" -> market))

  def getBalances: List[Balance] =
    makeRequest[List[Balance]]("GET", List("trading", "balance"))
}
