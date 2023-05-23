package org.olympus.hefesto

import breeze.linalg._
import breeze.optimize._

object Optimizer {
  implicit def arrayToOptimizer(s: Array[Double]): Optimizer = Optimizer(s)
}

case class Optimizer(start: Array[Double]) {
  def minimizeBy(objectiveFunction: Array[Double] => Double): Array[Double] = {
    val function = new ApproximateGradientFunction((x: DenseVector[Double]) => objectiveFunction(x.toArray))
    new LBFGS[DenseVector[Double]].minimize(function, DenseVector(start)).toArray
  }
}
