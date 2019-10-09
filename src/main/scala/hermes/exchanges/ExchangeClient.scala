package hermes.exchanges

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import hermes.exchanges.ExchangeModels._
import spray.json._

import scala.concurrent._
import scala.concurrent.duration._

object ExchangeClient {
  def apply(account: Account): ExchangeClient = account.exchange.toLowerCase match {
    case "binance" => binance.v3.BinanceClient(account.publicKey, account.privateKey)
    case "bittrex" => bittrex.v1.BittrexClient(account.publicKey, account.privateKey)
    case "hitbtc" => hitbtc.v2.HitbtcClient(account.publicKey, account.privateKey)
  }
}

trait ExchangeClient {
  val publicKey: String
  val privateKey: String
  val rateLimit: Long
  val host: String
  val path: String
  val fee: BigDecimal

  protected implicit val system = ActorSystem()
  protected implicit val materializer = ActorMaterializer()
  protected implicit val executionContext = system.dispatcher
  protected val http = Http()
  protected var lastRequest = System.currentTimeMillis

  def makeRequest[T: RootJsonFormat](method: String, route: List[String], params: Map[String, Any] = Map()): T = {
    Thread.sleep(Math.max(lastRequest + rateLimit - System.currentTimeMillis, 0))
    lastRequest = System.currentTimeMillis
    val httpRequest = Future(createHttpRequest(method, route, params))
    val httpResponse = httpRequest.flatMap(http.singleRequest(_))
    val response = httpResponse.flatMap(printResponse(lastRequest)).flatMap(handleHttpResponse[T])
    Await.result(response, Duration(30, SECONDS))
  }

  protected def printResponse(startTime: Long)(response: HttpResponse): Future[HttpResponse] = {
    response.entity.toStrict(Duration(30, SECONDS)).map { cachedEntity =>
      val header = s"RTT: ${System.currentTimeMillis - startTime}ms - ${response.status} - "
      cachedEntity.dataBytes.runFold(ByteString(header))(_ ++ _).map(_.utf8String).foreach(println)
      response.copy(entity = cachedEntity)
    }
  }

  protected def createHttpRequest(method: String, route: List[String], params: Map[String, Any]): HttpRequest
  protected def handleHttpResponse[T: RootJsonFormat](response: HttpResponse): Future[T]

  def getMarkets: List[Market]

  def getTickers: List[Ticker]

  def getOrderBook(market: String): OrderBook

  def getLastTrades(market: String): List[Trade]

  def getBalances: List[Balance]

  def getOpenOrders(market: String): List[OpenOrder]

  def sendOrder(market: String, side: OrderSide, price: BigDecimal, volume: BigDecimal): String

  def cancelOrder(orderId: String): Boolean

  def tryToSendOrder(market: Market, side: OrderSide, price: BigDecimal, volume: BigDecimal): Option[String] = {
    val realPrice = price.quot(market.tickPrice) * market.tickPrice
    val realVolume = volume.quot(market.tickBaseVolume) * market.tickBaseVolume
    val minConditions = realPrice * realVolume >= market.minQuoteVolume &&
        realPrice >= market.minPrice && realVolume >= market.minBaseVolume
    if (minConditions) Some(sendOrder(market.name, side, realPrice, realVolume)) else None
  }
}
