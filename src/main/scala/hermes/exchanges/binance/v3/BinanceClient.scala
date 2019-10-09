package hermes.exchanges.binance.v3

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.unmarshalling.Unmarshal
import hermes.enums.OrderSide
import hermes.exchanges.ExchangeClient
import hermes.exchanges.ExchangeModels._
import hermes.exchanges.binance.v3.BinanceCodecs._
import hermes.utils.Authenticator
import spray.json.RootJsonFormat

case class BinanceClient(publicKey: String, privateKey: String, rateLimit: Long = 50L) extends ExchangeClient {
  val host = "https://api.binance.com"
  val path = "/api"
  val fee = BigDecimal("0.0010")

  protected val authenticator = Authenticator(privateKey, "HmacSHA256")

  protected def createHttpRequest(method: String, route: List[String], params: Map[String, Any]) = route.head match {
    case "v1" =>
      val allParams = params.mapValues(_.toString)
      val uri = Uri(host).withPath(Uri.Path(s"$path/${route.mkString("/")}")).withQuery(Uri.Query(allParams))
      HttpRequest(HttpMethods.getForKey(method).get, uri, Nil)
    case "v3" =>
      val totalParams = (params + ("timestamp" -> System.currentTimeMillis)).mapValues(_.toString)
      val allParams = totalParams + ("signature" -> authenticator.sign(Uri.Query(totalParams).toString))
      val uri = Uri(host).withPath(Uri.Path(s"$path/${route.mkString("/")}")).withQuery(Uri.Query(allParams))
      val headers = List(RawHeader("X-MBX-APIKEY", publicKey))
      HttpRequest(HttpMethods.getForKey(method).get, uri, headers)
  }

  protected def handleHttpResponse[T: RootJsonFormat](response: HttpResponse) = response match {
    case HttpResponse(StatusCodes.OK, _, _, _) => Unmarshal(response).to[T]
    case _ => Unmarshal(response).to[Error].map {
      case Error(-2011, "Unknown order sent.") => None.asInstanceOf[T]
      case Error(_, message) => sys.error(message)
    }
  }

  def getMarkets: List[Market] =
    makeRequest[ExchangeInfo]("GET", List("v1", "exchangeInfo")).symbols

  def getTickers: List[Ticker] =
    makeRequest[List[Ticker]]("GET", List("v1", "ticker", "24hr"))

  def getOrderBook(market: String): OrderBook =
    makeRequest[OrderBook]("GET", List("v1", "depth"), Map("symbol" -> market, "limit" -> 100))

  def getLastTrades(market: String): List[Trade] =
    makeRequest[List[Trade]]("GET", List("v1", "trades"), Map("symbol" -> market, "limit" -> 100))

  def getBalances: List[Balance] =
    makeRequest[AccountInfo]("GET", List("v3", "account")).balances

  def getOpenOrders(market: String): List[OpenOrder] =
    makeRequest[List[OpenOrder]]("GET", List("v3", "openOrders"), Map("symbol" -> market))

  def sendOrder(market: String, side: OrderSide, price: BigDecimal, volume: BigDecimal): String =
    makeRequest[OpenOrder]("POST", List("v3", "order"), Map("symbol" -> market, "type" -> "LIMIT",
      "timeInForce" -> "GTC", "side" -> side.toString.toUpperCase, "price" -> price, "quantity" -> volume)).id

  def cancelOrder(orderId: String): Boolean =
    makeRequest[Option[OpenOrder]]("DELETE", List("v3", "order"),
      Map("symbol" -> orderId.split(" ").head, "orderId" -> orderId.split(" ").last)).nonEmpty
}
