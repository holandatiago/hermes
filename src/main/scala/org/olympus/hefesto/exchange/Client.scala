package org.olympus.hefesto.exchange

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.olympus.hefesto.exchange.Models._
import spray.json.RootJsonFormat

import scala.concurrent._
import scala.concurrent.duration._

object Client {
  val host: Uri = Uri("https://eapi.binance.com")
  val path: Uri.Path = Uri.Path./("eapi")./("v1")
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private def makeRequest[T: RootJsonFormat](route: String, params: Map[String, Any] = Map()): T = {
    val uri = host.withPath(path./(route)).withQuery(Uri.Query(params.mapValues(_.toString)))
    Await.result(Http().singleRequest(HttpRequest(uri = uri)).flatMap(Unmarshal(_).to[T]), Duration(30, SECONDS))
  }

  def fetchUnderlyingPrice(underlying: String): UnderlyingAsset =
    makeRequest[UnderlyingAsset]("index", Map("underlying" -> underlying)).copy(underlying = underlying)

  def fetchOptionPrices: List[OptionAsset] = makeRequest[List[OptionAsset]]("mark")

  def fetchMarketInfo: MarketInfo = makeRequest[MarketInfo]("exchangeInfo")

  def fetchMarketPrices: List[UnderlyingAsset] = {
    val marketInfo = fetchMarketInfo
    val optionPrices = fetchOptionPrices.groupBy(_.symbol).mapValues(_.head)
    val options = marketInfo.optionInfo
      .map(option => option.copy(price = optionPrices(option.symbol).price))
      .map(option => option.copy(volatility = optionPrices(option.symbol).volatility))
    marketInfo.underlyingInfo.sortBy(_.underlying)
      .map(asset => asset.copy(spot = fetchUnderlyingPrice(asset.underlying).spot))
      .map(asset => asset.copy(options = options.filter(_.underlying == asset.underlying).sortBy(_.symbol)))
  }
}
