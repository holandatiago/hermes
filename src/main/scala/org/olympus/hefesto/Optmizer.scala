package org.olympus.hefesto

import breeze.linalg._
import breeze.optimize._

import scala.collection.immutable.Vector

object Optmizer {
  val engine = new LBFGS[DenseVector[Double]]

  def minimize(objectiveFunction: Vector[Double] => Double, start: Vector[Double]): Vector[Double] = {
    val function = new ApproximateGradientFunction((x: DenseVector[Double]) => objectiveFunction(x.toScalaVector))
    engine.minimize(function, DenseVector(start.toArray)).toScalaVector
  }
}
