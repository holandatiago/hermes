package org.olympus.hefesto

import com.opengamma.strata.pricer.impl.option._
import org.olympus.hefesto.Models._
import org.olympus.hefesto.Plotter._

import scala.util.Try

object Main extends App {
  val marketPrices = Client.fetchMarketPrices
  marketPrices.foreach(prettyPrinter)
  marketPrices.foreach(pricesPrinter)
  marketPrices.foreach(volsPrinter)
  marketPrices.foreach(volsPlotter)

  def volsPlotter(asset: UnderlyingAsset): Unit = {
    asset.options
      .plot(_.strike, _.volatility).groupBy(_.side).splitBy(_.term)
      .addCurve("SMILE", t => t*t).addVertical("SPOT", asset.spot)
      .centeredIn(asset.spot, logarithmic = true)
      .withinLimits((-1, 1), (0, 2)).display(asset.underlying)
  }

  def prettyPrinter(asset: UnderlyingAsset): Unit = {
    println(s"${asset.underlying}\tSPOT: %.8f".format(asset.spot))
    asset.options.groupBy(_.term).toList.sortBy(_._1.toEpochDay).foreach { case (term, options) =>
      println(s"\t$term")
      options.groupBy(_.strike).toList.sortBy(_._1).foreach { case (strike, options) =>
        val List(call, put) = options.sortBy(_.side.toString)
        val printFormat = s"\t\tSTRIKE: %5.0f\tPRICES: %8.2f/C %8.2f/P\tVOLS: %6.4f/C %6.4f/P"
        println(printFormat.format(strike, call.price, put.price, call.volatility, put.volatility))
      }
    }
  }

  def pricesPrinter(asset: UnderlyingAsset): Unit = {
    asset.options.foreach { option =>
      val blackPrice = BlackScholesFormulaRepository
        .price(asset.spot, option.strike, option.timeToExpiry, option.volatility, 0, 0, option.side == OptionSide.CALL)
      val printFormat = s"${option.symbol}\tPRICES: %8.2f %8.2f\t%6.4f"
      println(printFormat.format(option.price, blackPrice, blackPrice / option.price))
    }
  }

  def volsPrinter(asset: UnderlyingAsset): Unit = {
    asset.options.foreach { option =>
      val solver = new GenericImpliedVolatiltySolver(volatility => Array(
        BlackScholesFormulaRepository
          .price(asset.spot, option.strike, option.timeToExpiry, volatility, 0, 0, option.side == OptionSide.CALL),
        BlackScholesFormulaRepository
          .vega(asset.spot, option.strike, option.timeToExpiry, volatility, 0, 0)))
      val blackVol = Try(solver.impliedVolatility(option.price)).getOrElse(Double.NaN)
      val printFormat = s"${option.symbol}\tVOLS: %8.4f %8.4f\t%8.4f"
      println(printFormat.format(option.volatility, blackVol, blackVol / option.volatility))
    }
  }
}
