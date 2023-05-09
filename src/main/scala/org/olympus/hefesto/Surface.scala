package org.olympus.hefesto

import org.olympus.hefesto.Models.UnderlyingAsset

case class Surface(sigma: Double, rho: Double, eta: Double, rate: Double) {
  def volatility(k: Double, t: Double): Double = {
    val theta = sigma * sigma * t
    val phi = eta / Math.sqrt(theta)
    val phik = phi * (k + rate * t)
    val w = 0.5 * theta * (1.0 + rho * phik + Math.sqrt(1.0 + (2 * rho + phik) * phik))
    Math.sqrt(w / t)
  }

  def objectiveFunction(asset: UnderlyingAsset): Double = {
    asset.options
      .map(option => (option.volatility - volatility(option.logMoneyness, option.timeToExpiry)) / option.spread)
      .map(error => error * error).sum / asset.options.size
  }
}

object Surface {
  def calibrate(asset: UnderlyingAsset): Surface = {
    var surface = Surface(1, 0, 1, 0)
    for (power <- 1 to 3) {
      val range = (-9 to 9).map(_ * Math.pow(10, -power))
      val ss = for {
        sigma <- range.map(_ + surface.sigma)
        rho <- range.map(_ + surface.rho)
        eta <- range.map(_ + surface.eta)
        rate <- range.map(_ + surface.rate)
      } yield Surface(sigma, rho, eta, rate)
      surface = ss.minBy(_.objectiveFunction(asset))
    }
    surface
  }
}
