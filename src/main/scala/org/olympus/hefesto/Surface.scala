package org.olympus.hefesto

import org.apache.commons.math3.fitting.leastsquares._
import org.apache.commons.math3.linear.DiagonalMatrix
import org.olympus.hefesto.Models.UnderlyingAsset

case class Surface(sigma: Double, rho: Double, eta: Double) {
  def this(params: Array[Double]) = this(params(0), params(1), params(2))
  val params: Array[Double] = Array(sigma, rho, eta)

  def volatility(k: Double, t: Double): Double = {
    val theta = sigma * sigma * t
    val phi = eta / Math.sqrt(theta)
    val sqrt = Math.sqrt(1 + phi * k * (2 * rho + phi * k))
    val w = theta / 2 * (1 + phi * k * rho + sqrt)
    Math.sqrt(w / t)
  }

  def jacobian(k: Double, t: Double): Array[Double] = {
    val theta = sigma * sigma * t
    val phi = eta / Math.sqrt(theta)
    val sqrt = Math.sqrt(1 + phi * k * (2 * rho + phi * k))
    val w = theta / 2 * (1 + phi * k * rho + sqrt)

    val wBar = 0.5 / Math.sqrt(w * t)
    val phiBar = wBar * theta / 2 * k * (rho + (rho + phi * k) / sqrt)
    val thetaBar = wBar * w / theta + phiBar * -0.5 * eta / Math.sqrt(theta) / theta

    val sigmaBar = thetaBar * 2 * sigma * t
    val rhoBar = wBar * theta / 2 * phi * k * (1 + 1 / sqrt)
    val etaBar = phiBar / Math.sqrt(theta)

    Array(sigmaBar, rhoBar, etaBar)
  }

  def volatilities(asset: UnderlyingAsset): Array[Double] = {
    asset.options.map(option => volatility(option.logMoneyness, option.timeToExpiry)).toArray
  }

  def jacobians(asset: UnderlyingAsset): Array[Array[Double]] = {
    asset.options.map(option => jacobian(option.logMoneyness, option.timeToExpiry)).toArray
  }

  def objectiveFunction(asset: UnderlyingAsset): Double = {
    val errors = asset.options
      .map(option => (option.volatility - volatility(option.logMoneyness, option.timeToExpiry)) / option.spread)
    Math.sqrt(errors.map(error => error * error).sum / asset.options.size)
  }
}

object Surface {
  def calibrate(asset: UnderlyingAsset): Surface = {
    val problem = new LeastSquaresBuilder()
      .start(Surface(1, 0, 1).params)
      .target(asset.options.map(_.volatility).toArray)
      .model(new Surface(_).volatilities(asset), new Surface(_).jacobians(asset))
      .weight(new DiagonalMatrix(asset.options.map(_.spread).map(w => 1 / (w * w)).toArray))
      .lazyEvaluation(false).maxEvaluations(1000).maxIterations(1000).build
    new Surface(new LevenbergMarquardtOptimizer().optimize(problem).getPoint.toArray)
  }
}
