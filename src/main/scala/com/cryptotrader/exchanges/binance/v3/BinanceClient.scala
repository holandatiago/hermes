package com.cryptotrader.exchanges.binance.v3

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.cryptotrader.exchanges._
import com.cryptotrader.exchanges.binance.v3.BinanceModels._
import com.cryptotrader.exchanges.utils._
import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

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

  override def handleHttpResponse[T: JsonFormat](response: HttpResponse) = {
    Unmarshal(response).to[BittrexResponse[T]].map(_.result.get)
  }

  def getMarkets: List[ExchangeModels.Market] =
    makeRequest[List[Market]]("GET", List("v1", "exchangeInfo"))

  def getTickers: List[ExchangeModels.Ticker] =
    makeRequest[List[MarketSummary]]("GET", List("v1", "ticker", "24hr"))

  def getOrderBook(market: String): ExchangeModels.OrderBook =
    makeRequest[OrderBook]("GET", List("v1", "depth"), Map("symbol" -> market))

  def getLastTrades(market: String): List[ExchangeModels.Trade] =
    makeRequest[List[Trade]]("GET", List("v1", "trades"), Map("symbol" -> market))

  def sendOrder(market: String, side: String, price: BigDecimal, volume: BigDecimal): Unit =
    makeRequest[Uuid]("POST", List("v3", "order"),
      Map("symbol" -> market, "side" -> side, "type" -> "limit", "price" -> price, "quantity" -> volume))

  def cancelOrder(orderId: String): Unit =
    makeRequest[Uuid]("DELETE", List("v3", "order"), Map("orderId" -> orderId))

  def getOpenOrders(market: String): List[ExchangeModels.Order] =
    makeRequest[List[OpenOrder]]("GET", List("v3", "openOrders"), Map("symbol" -> market))

  def getBalances: List[ExchangeModels.Balance] =
    makeRequest[List[Balance]]("GET", List("v3", "account"))
}
