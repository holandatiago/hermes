package hermes.exchanges

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import hermes.exchanges.ExchangeModels._
import hermes.exchanges.utils._
import spray.json._

import scala.concurrent._
import scala.concurrent.duration._

object ExchangeClient {
  def apply(name: String, apiKey: ApiKey): ExchangeClient = name.toLowerCase match {
    case "binance" => new binance.v3.BinanceClient(apiKey)
    case "bittrex" => new bittrex.v1.BittrexClient(apiKey)
    case "hitbtc" => new hitbtc.v2.HitbtcClient(apiKey)
  }
}

trait ExchangeClient {
  //make it abstract class and its childs case classes?
  //make those variables a parameter? Put on object?
  protected implicit val system = ActorSystem()
  protected implicit val materializer = ActorMaterializer()
  protected implicit val executionContext = system.dispatcher
  protected val http = Http()
  val apiKey: ApiKey

  protected def buildHttpRequest(method: String, route: List[String], params: Map[String, Any] = Map()): HttpRequest
  protected def handleHttpResponse[T: RootJsonFormat](response: HttpResponse): Future[T]

  def makeRequest[T: RootJsonFormat](method: String, route: List[String], params: Map[String, Any] = Map()): T = {
    val httpRequest = buildHttpRequest(method, route, params)
    val httpResponse = http.singleRequest(httpRequest)
    val response = httpResponse.flatMap(handleHttpResponse[T])
    Await.result(response, Duration(30, SECONDS))
  }

  def getMarkets: List[Market]

  def getTickers: List[Ticker]

  def getOrderBook(market: String): OrderBook

  def getLastTrades(market: String): List[Trade]

  def sendOrder(market: String, side: OrderSide, price: BigDecimal, volume: BigDecimal): Unit

  def cancelOrder(orderId: String): Unit

  def getOpenOrders(market: String): List[OpenOrder]

  def getBalances: List[Balance]
}
