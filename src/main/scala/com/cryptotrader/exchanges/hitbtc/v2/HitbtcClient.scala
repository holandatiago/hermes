package com.cryptotrader.exchanges.hitbtc.v2

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.cryptotrader.exchanges.ExchangeClient
import com.cryptotrader.exchanges.ExchangeModels._
import com.cryptotrader.exchanges.hitbtc.v2.HitbtcCodecs._
import com.cryptotrader.exchanges.utils._
import spray.json.RootJsonFormat

class HitbtcClient(apiKey: ApiKey) extends ExchangeClient {
  val auth = Authorization(BasicHttpCredentials(apiKey.public, apiKey.secret))
  val host = "https://api.hitbtc.com"
  val path = "/api/2"

  override def buildHttpRequest(method: String, route: List[String], params: Map[String, Any]) = {
    val allParams = params.mapValues(_.toString)
    val uri = Uri(host).withPath(Uri.Path(s"$path/${route.mkString("/")}")).withQuery(Uri.Query(allParams))
    val headers = List(auth)
    HttpRequest(HttpMethods.getForKey(method).get, uri, headers)
  }

  override def handleHttpResponse[T: RootJsonFormat](response: HttpResponse) = {
    Unmarshal(response).to[T]
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
