package com.holandatiago.cryptotrader.exchanges.bittrex.v1

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.holandatiago.cryptotrader.exchanges.ExchangeClient
import com.holandatiago.cryptotrader.exchanges.bittrex.v1.models._
import com.holandatiago.cryptotrader.exchanges.utils._
import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

import scala.concurrent._
import scala.concurrent.duration._

class BittrexClient(apiKey: ApiKey) extends ExchangeClient {
  implicit val system = ActorSystem()
  implicit val materializer =ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val http = Http()
  val auth = Auth(apiKey.secret, "HmacSHA512")
  val host = "https://api.bittrex.com"
  val path = "/api/v1.1"

  def futureString(response: HttpResponse): Future[String] = {
    response.entity.dataBytes.runFold(akka.util.ByteString(""))(_ ++ _).map(_.utf8String)
  }

  def get[T: JsonFormat](route: String, method: String, params: Map[String, String] = Map()): T = {
    val signed = true //route != "public"
    val apiKeyParams = Map("apiKey" -> apiKey.key, "nonce" -> System.currentTimeMillis().toString)
    val allParams = if (signed) apiKeyParams ++ params else params
    val uri = Uri(host).withPath(Uri.Path(s"$path/$route/$method")).withQuery(Uri.Query(allParams))
    val headers = if (signed) List(RawHeader("apisign", auth.generateHmac(uri.toString))) else Nil
    val httpRequest = HttpRequest(uri = uri, headers = headers)
    val httpResponse = http.singleRequest(httpRequest)
    val response = httpResponse.flatMap(r => Unmarshal(r).to[BittrexResponse[T]]).map(_.result.get)
    Await.result(response, Duration(30, SECONDS))
  }

  def getMarkets =
    get[List[Market]]("public", "getmarkets")

  def getTickers =
    get[List[MarketSummary]]("public", "getmarketsummaries")

  def getOrderBook(market: String) =
    get[OrderBook]("public", "getorderbook", Map("market" -> market, "type" -> "both"))

  def getTrades(market: String) =
    get[List[Trade]]("public", "getmarkethistory", Map("market" -> market))

  def sendOrder(market: String, action: String, price: BigDecimal, volume: BigDecimal) =
    get[Uuid]("market", s"${action}limit",
      Map("market" -> market, "rate" -> price.toString, "quantity" -> volume.toString))

  def cancelOrder(orderId: String) =
    get[Uuid]("market", "cancel", Map("uuid" -> orderId))

  def getOpenOrders(market: String) =
    get[List[OpenOrder]]("market", "getopenorders", Map("market" -> market))

  def getBalances =
    get[List[Balance]]("account", "getbalances")
}
