package com.cryptotrader.exchanges.bittrex.v1

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.cryptotrader.exchanges.ExchangeClient
import com.cryptotrader.exchanges.ExchangeModels._
import com.cryptotrader.exchanges.bittrex.v1.BittrexCodecs._
import com.cryptotrader.exchanges.utils._
import spray.json.RootJsonFormat

class BittrexClient(apiKey: ApiKey) extends ExchangeClient {
  val auth = Auth(apiKey.secret, "HmacSHA512")
  val host = "https://api.bittrex.com"
  val path = "/api/v1.1"

  override def buildHttpRequest(method: String, route: List[String], params: Map[String, Any]) = {
    val apiKeyParams = Map("apiKey" -> apiKey.public, "nonce" -> System.currentTimeMillis())
    val allParams = (params ++ apiKeyParams).mapValues(_.toString)
    val uri = Uri(host).withPath(Uri.Path(s"$path/${route.mkString("/")}")).withQuery(Uri.Query(allParams))
    val headers = List(RawHeader("apisign", auth.generateHmac(uri.toString)))
    HttpRequest(HttpMethods.getForKey(method).get, uri, headers)
  }

  override def handleHttpResponse[T: RootJsonFormat](response: HttpResponse) = {
    Unmarshal(response).to[Response[T]].map(_.result.get)
  }

  def getMarkets: List[Market] =
    makeRequest[List[Market]]("GET", List("public", "getmarkets"))

  def getTickers: List[Ticker] =
    makeRequest[List[Ticker]]("GET", List("public", "getmarketsummaries"))

  def getOrderBook(market: String): OrderBook =
    makeRequest[OrderBook]("GET", List("public", "getorderbook"),
      Map("market" -> market, "type" -> "both"))

  def getLastTrades(market: String): List[Trade] =
    makeRequest[List[Trade]]("GET", List("public", "getmarkethistory"), Map("market" -> market))

  def sendOrder(market: String, side: OrderSide, price: BigDecimal, volume: BigDecimal): Unit =
    makeRequest[Option[Nothing]]("GET", List("market", s"${side}limit"),
      Map("market" -> market, "rate" -> price, "quantity" -> volume))

  def cancelOrder(orderId: String): Unit =
    makeRequest[Option[Nothing]]("GET", List("market", "cancel"), Map("uuid" -> orderId))

  def getOpenOrders(market: String): List[OpenOrder] =
    makeRequest[List[OpenOrder]]("GET", List("market", "getopenorders"), Map("market" -> market))

  def getBalances: List[Balance] =
    makeRequest[List[Balance]]("GET", List("account", "getbalances"))
}
