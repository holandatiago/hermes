package org.olympus.hefesto

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.olympus.hefesto.Models._
import spray.json._

import java.time._
import scala.concurrent._
import scala.concurrent.duration._

object Client extends DefaultJsonProtocol {
  val host: Uri = Uri("https://eapi.binance.com")
  val path: Uri.Path = Uri.Path./("eapi")./("v1")
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private def makeRequest[T: RootJsonFormat](route: String, params: Map[String, Any] = Map()): T = {
    val uri = host.withPath(path./(route)).withQuery(Uri.Query(params.mapValues(_.toString)))
    Await.result(Http().singleRequest(HttpRequest(uri = uri)).flatMap(Unmarshal(_).to[T]), (30, SECONDS))
  }

  def fetchMarketPrices: List[UnderlyingAsset] = {
    val marketInfo = fetchMarketInfo
    val optionPrices = fetchOptionPrices.groupBy(_.symbol).mapValues(_.head)
    val options = marketInfo.optionInfo
      .map(option => option.copy(price = optionPrices(option.symbol).price))
      .map(option => option.copy(volatility = optionPrices(option.symbol).volatility))
      .map(option => option.copy(timeToExpiry = calculateTimeToExpiry(option.term, marketInfo.currentTime)))
    marketInfo.underlyingInfo.sortBy(_.underlying)
      .map(asset => asset.copy(spot = fetchUnderlyingPrice(asset.underlying).spot))
      .map(asset => asset.copy(options = options.filter(_.underlying == asset.underlying).sortBy(_.symbol)))
  }

  def fetchMarketInfo: MarketInfo = {
    implicit val underlyingInfoCodec: RootJsonFormat[UnderlyingAsset] = lift((json: JsValue) => UnderlyingAsset(
      underlying = fromField[String](json, "underlying"),
      baseAsset = fromField[String](json, "baseAsset"),
      quoteAsset = fromField[String](json, "quoteAsset"),
      spot = Double.NaN,
      options = null))
    implicit val optionInfoCodec: RootJsonFormat[OptionAsset] = lift((json: JsValue) => OptionAsset(
      symbol = fromField[String](json, "symbol"),
      underlying = fromField[String](json, "underlying"),
      term = LocalDate.ofInstant(Instant.ofEpochMilli(fromField[Long](json, "expiryDate")), ZoneOffset.UTC),
      timeToExpiry = Double.NaN,
      strike = fromField[BigDecimal](json, "strikePrice").doubleValue(),
      side = List(OptionSide.CALL, OptionSide.PUT).find(_.toString == fromField[String](json, "side")).get,
      price = Double.NaN,
      volatility = Double.NaN))
    implicit val marketInfoCodec: RootJsonFormat[MarketInfo] = lift((json: JsValue) => MarketInfo(
      currentTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fromField[Long](json, "serverTime")), ZoneOffset.UTC),
      underlyingInfo = fromField[List[UnderlyingAsset]](json, "optionContracts"),
      optionInfo = fromField[List[OptionAsset]](json, "optionSymbols")))
    makeRequest[MarketInfo]("exchangeInfo")
  }

  def fetchUnderlyingPrice(underlying: String): UnderlyingAsset = {
    implicit val underlyingPriceCodec: RootJsonFormat[UnderlyingAsset] = lift((json: JsValue) => UnderlyingAsset(
      underlying = null,
      baseAsset = null,
      quoteAsset = null,
      spot = fromField[BigDecimal](json, "indexPrice").doubleValue(),
      options = null))
    makeRequest[UnderlyingAsset]("index", Map("underlying" -> underlying)).copy(underlying = underlying)
  }

  def fetchOptionPrices: List[OptionAsset] = {
    implicit val optionPriceCodec: RootJsonFormat[OptionAsset] = lift((json: JsValue) => OptionAsset(
      symbol = fromField[String](json, "symbol"),
      underlying = null,
      term = null,
      timeToExpiry = Double.NaN,
      strike = Double.NaN,
      side = null,
      price = fromField[BigDecimal](json, "markPrice").doubleValue(),
      volatility = fromField[BigDecimal](json, "markIV").doubleValue()))
    makeRequest[List[OptionAsset]]("mark")
  }

  def calculateTimeToExpiry(term: LocalDate, currentTime: LocalDateTime): Double = {
    val termEpochSecond = term.atTime(8, 0).toEpochSecond(ZoneOffset.UTC).toDouble
    val currentEpochSecond = currentTime.toEpochSecond(ZoneOffset.UTC).toDouble
    (termEpochSecond - currentEpochSecond) / (365 * 24 * 60 * 60)
  }
}
