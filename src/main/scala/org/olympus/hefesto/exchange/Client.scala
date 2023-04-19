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

  def getOptionPrice(symbol: String): OptionPrice =
    makeRequest[List[OptionPrice]]("mark", Map("symbol" -> symbol)).head

  def getSpotPrice(symbol: String): SpotPrice =
    makeRequest[SpotPrice]("index", Map("underlying" -> symbol)).copy(symbol = symbol)
}
