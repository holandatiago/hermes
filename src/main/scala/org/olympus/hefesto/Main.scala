package org.olympus.hefesto

import com.opengamma.strata.pricer.impl.option._
import org.olympus.hefesto.exchange.Client
import org.olympus.hefesto.exchange.Models.OptionSide

import java.time._
import scala.util.Try

object Main extends App {
  val secondsPerYear = 31536000D
  val nowEpochSecond = ZonedDateTime.now().toEpochSecond

  Client.fetchMarketPrices.foreach { asset => asset.options.foreach { option =>
    val termEpochSecond = option.term.atTime(8, 0).toEpochSecond(ZoneOffset.UTC)
    val timeToExpiry = (termEpochSecond - nowEpochSecond) / secondsPerYear
    val isCall = option.side == OptionSide.CALL
    val blackPrice = BlackScholesFormulaRepository.price(asset.spot, option.strike, timeToExpiry, option.volatility, 0, 0, isCall)
    println(s"${option.symbol}\tPRICES: %8.2f %8.2f\t%6.4f".format(option.price, blackPrice, blackPrice/option.price))
  } }

  Client.fetchMarketPrices.foreach { asset => asset.options.foreach { option =>
    val termEpochSecond = option.term.atTime(8, 0).toEpochSecond(ZoneOffset.UTC)
    val timeToExpiry = (termEpochSecond - nowEpochSecond) / secondsPerYear
    val isCall = option.side == OptionSide.CALL
    val solver = new GenericImpliedVolatiltySolver(vol => Array(
      BlackScholesFormulaRepository.price(asset.spot, option.strike, timeToExpiry, vol, 0, 0, isCall),
      BlackScholesFormulaRepository.vega(asset.spot, option.strike, timeToExpiry, vol, 0, 0)))
    val blackVol = Try(solver.impliedVolatility(option.price)).getOrElse(Double.NaN)
    println(s"${option.symbol}\tVOLS: %8.4f %8.4f\t%8.4f".format(option.volatility, blackVol, blackVol / option.volatility))
  } }
}
