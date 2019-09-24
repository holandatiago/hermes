package com.cryptotrader.exchanges.hitbtc.v2

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.cryptotrader.exchanges._
import com.cryptotrader.exchanges.hitbtc.v2.HitbtcModels._
import com.cryptotrader.exchanges.utils._
import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

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

  override def handleHttpResponse[T: JsonFormat](response: HttpResponse) = {
    Unmarshal(response).to[BittrexResponse[T]].map(_.result.get)
  }

  def getMarkets: List[ExchangeModels.Market] =
    makeRequest[List[Market]]("GET", List("public", "symbol"))

  def getTickers: List[ExchangeModels.Ticker] =
    makeRequest[List[MarketSummary]]("GET", List("public", "ticker"))

  def getOrderBook(market: String): ExchangeModels.OrderBook =
    makeRequest[OrderBook]("GET", List("public", "orderbook", market))

  def getLastTrades(market: String): List[ExchangeModels.Trade] =
    makeRequest[List[Trade]]("GET", List("public", "trades", market))

  def sendOrder(market: String, side: String, price: BigDecimal, volume: BigDecimal): Unit =
    makeRequest[Uuid]("POST", List("order"),
      Map("symbol" -> market, "side" -> side, "price" -> price, "quantity" -> volume))

  def cancelOrder(orderId: String): Unit =
    makeRequest[Uuid]("DELETE", List("order", orderId))

  def getOpenOrders(market: String): List[ExchangeModels.Order] =
    makeRequest[List[OpenOrder]]("GET", List("order"), Map("symbol" -> market))

  def getBalances: List[ExchangeModels.Balance] =
    makeRequest[List[Balance]]("GET", List("trading", "balance"))
}
