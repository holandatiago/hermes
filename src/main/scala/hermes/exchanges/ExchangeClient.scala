package hermes.exchanges

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import hermes.exchanges.ExchangeModels._
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter
import spray.json._

import scala.concurrent._
import scala.concurrent.duration._

object ExchangeClient {
  //delete this shit?
  case class ApiKey(public: String, secret: String)

  //merge with Exchange client? Trait or Object?
  case class Auth(key: String, algorithm: String) {
    val secret = new SecretKeySpec(key.getBytes, algorithm)
    val mac = Mac.getInstance(algorithm)
    mac.init(secret)

    def generateHmac(message: String): String = {
      val hash = mac.doFinal(message.getBytes)
      DatatypeConverter.printHexBinary(hash)
    }
  }

  //put it in ExchangeClient? make function2JsonReader then JsonReader2RootJsonFormat?
  implicit def readFunc2JsonFormat[T](implicit f: JsValue => T) = new RootJsonFormat[T] {
    def write(obj: T) = JsNull
    def read(json: JsValue) = f(json)
  }

  //remove that Option[Nothing] shit
  implicit val emptyCodec = DefaultJsonProtocol.jsonFormat0[Option[Nothing]](() => None)

  def apply(name: String): ExchangeClient = apply(Account(name))

  def apply(account: Account): ExchangeClient = account.exchange.toLowerCase match {
    case binance.v3.BinanceClient.name => new binance.v3.BinanceClient(ApiKey(account.publicKey, account.privateKey))
    case bittrex.v1.BittrexClient.name => new bittrex.v1.BittrexClient(ApiKey(account.publicKey, account.privateKey))
    case hitbtc.v2.HitbtcClient.name => new hitbtc.v2.HitbtcClient(ApiKey(account.publicKey, account.privateKey))
  }
}

trait ExchangeClient {
  import ExchangeClient._
  //make it abstract class and its childs case classes?
  //make those variables a parameter? Put on object?
  protected implicit val system = ActorSystem()
  protected implicit val materializer = ActorMaterializer()
  protected implicit val executionContext = system.dispatcher
  protected val http = Http()
  val apiKey: ApiKey

  protected def buildHttpRequest(method: String, route: List[String], params: Map[String, Any] = Map()): HttpRequest
  protected def handleHttpResponse[T: RootJsonFormat](response: HttpResponse): Future[T]

  protected def printResponse(response: HttpResponse): Future[HttpResponse] = {
    response.entity.toStrict(Duration(30, SECONDS)).map { cachedEntity =>
      cachedEntity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String).foreach(println)
      response.copy(entity = cachedEntity)
    }
  }

  def makeRequest[T: RootJsonFormat](method: String, route: List[String], params: Map[String, Any] = Map()): T = {
    val httpRequest = buildHttpRequest(method, route, params)
    val httpResponseFuture = http.singleRequest(httpRequest)
    val response = httpResponseFuture.flatMap(printResponse).flatMap(handleHttpResponse[T])
    Await.result(response, Duration(30, SECONDS))
  }

  def getMarkets: List[Market]

  def getTickers: List[Ticker]

  def getOrderBook(market: String): OrderBook

  def getLastTrades(market: String): List[Trade]

  def getBalances: List[Balance]

  def getOpenOrders(market: String): List[OpenOrder]

  def sendOrder(market: String, side: OrderSide, price: BigDecimal, volume: BigDecimal): Unit

  def cancelOrder(orderId: String): Unit
}
