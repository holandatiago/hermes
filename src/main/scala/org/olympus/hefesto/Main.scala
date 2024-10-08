package org.olympus.hefesto

import org.olympus.hefesto.Models._
import org.olympus.hefesto.Plotter._

object Main extends App {
  val marketPrices = Client.fetchMarketPrices
  marketPrices.foreach(volsPlotter)

  def volsPlotter(asset: UnderlyingAsset): Unit = if (asset.options.nonEmpty) {
    val surface = Surface.calibrate(asset)
    asset.options
      .plot(_.logMoneyness, _.volatility).deviateBy(_.spread).groupBy(_.side).splitBy(_.timeToExpiry)
      .addCurve("SMILE", surface.volatility).withinLimits((-1, 1), (0, 2)).display(asset.symbol)
    println(s"Plotted ${asset.symbol}\t${surface.rootMeanSquareError(asset)}\t$surface")
  } else println(s"isEmpty ${asset.symbol}")
}
