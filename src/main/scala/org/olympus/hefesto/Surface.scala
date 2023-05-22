package org.olympus.hefesto

import org.olympus.hefesto.Models.UnderlyingAsset

case class Surface(params: Vector[Double]) {
  val Vector(sigma, rho, eta) = params

  def volatility(k: Double, t: Double): Double = {
    val theta = sigma * sigma * t
    val phi = eta / Math.sqrt(theta)
    val sqrt = Math.sqrt(1 + phi * k * (2 * rho + phi * k))
    val w = theta / 2 * (1 + phi * k * rho + sqrt)
    Math.sqrt(w / t)
  }

  def rootMeanSquareError(asset: UnderlyingAsset): Double = {
    val errors = asset.options
      .map(option => (option.volatility - volatility(option.logMoneyness, option.timeToExpiry)) / option.spread)
    Math.sqrt(errors.map(error => error * error).sum / asset.options.size)
  }
}

object Surface {
  def calibrate(asset: UnderlyingAsset): Surface = {
    Surface(Optmizer.minimize(Surface(_).rootMeanSquareError(asset), Vector(1, 0, 1)))
  }
}
