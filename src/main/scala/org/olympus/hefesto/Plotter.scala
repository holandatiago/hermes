package org.olympus.hefesto

import org.knowm.xchart.XYSeries.XYSeriesRenderStyle
import org.knowm.xchart._
import org.knowm.xchart.style.Styler.ChartTheme
import org.knowm.xchart.style.markers.SeriesMarkers

import scala.collection.JavaConverters._

object Plotter {
  implicit def iterableToPlotter[T](x: Iterable[T]): Plotter[T] = new Plotter[T](x.toList)
}

case class Plotter[T](
    data: List[T],
    xMapper: T => Double = null,
    yMapper: T => (Double, Double) = null,
    zGrouper: T => Any = (_: T) => "",
    wSplitter: T => Any = (_: T) => "",
    uLines: List[(String, Double => Double)] = Nil,
    vLines: List[(String, Double)] = Nil,
    xLimits: (Double, Double) = null,
    yLimits: (Double, Double) = null,
    tFwd: Double => Double = identity,
    tBwd: Double => Double = identity) {
  def plot(xMapper: T => Double, yMapper: T => (Double, Double)): Plotter[T] = copy(xMapper = xMapper, yMapper = yMapper)
  def groupBy(zGrouper: T => Any): Plotter[T] = copy(zGrouper = zGrouper)
  def splitBy(wSplitter: T => Any): Plotter[T] = copy(wSplitter = wSplitter)
  def addCurve(uName: String, uFunc: Double => Double): Plotter[T] = copy(uLines = (uName, uFunc) :: uLines)
  def addVertical(vName: String, vValue: Double): Plotter[T] = copy(vLines = (vName, vValue) :: vLines)
  def withinLimits(xLim: (Double, Double), yLim: (Double, Double)): Plotter[T] = copy(xLimits = xLim, yLimits = yLim)
  def centeredIn(center: Double, logarithmic: Boolean = false): Plotter[T] = copy(
    tFwd = if (logarithmic) x => Math.log(x) - Math.log(center) else x => x - center,
    tBwd = if (logarithmic) t => Math.exp(t + Math.log(center)) else t => t + center)

  def display(title: String = ""): Unit = if (data.nonEmpty) {
    val xtMapper = xMapper andThen tFwd
    val (minXValue, maxXValue) = if (xLimits == null) (data.map(xtMapper).min, data.map(xtMapper).max) else xLimits
    val (minYValue, maxYValue) = if (yLimits == null) (data.map(yMapper).map(_._1).min, data.map(yMapper).map(_._2).max) else yLimits
    val step = (maxXValue - minXValue) / 20
    val range = Range.BigDecimal.inclusive(minXValue - step, maxXValue + step, step).toList.map(_.toDouble)

    val charts = data
      .groupBy(wSplitter)
      .toList.sortBy(_._1.toString)
      .map { case (part, wValues) =>
        val chart = new XYChart(800, 600, ChartTheme.GGPlot2)
        chart.setTitle(part.toString)
        chart.getStyler.setCursorEnabled(true)
        chart.getStyler.setCustomCursorXDataFormattingFunction(t => tBwd(t).round.toString)
        chart.getStyler
          .setXAxisMin(minXValue).setXAxisMax(maxXValue)
          .setYAxisMin(minYValue).setYAxisMax(maxYValue)
        vLines.foreach { case (vName, vValue) =>
          chart.addAnnotation(new AnnotationLine(tFwd(vValue), true, false))
          chart.addAnnotation(new AnnotationText(vName, tFwd(vValue), minYValue, false))
        }
        uLines.foreach { case (uName, uFunc) =>
          chart
            .addSeries(uName, range.asJava, range.map(uFunc).map(Double.box).asJava)
            .setXYSeriesRenderStyle(XYSeriesRenderStyle.Line).setMarker(SeriesMarkers.NONE)
        }
        wValues
          .groupBy(zGrouper)
          .toList.sortBy(_._1.toString)
          .foreach { case (group, zValues) =>
            chart
              .addSeries(group.toString, zValues.map(xtMapper).asJava,
                zValues.map(yMapper).map(y => (y._2 + y._1) / 2).map(Double.box).asJava,
                zValues.map(yMapper).map(y => (y._2 - y._1) / 2).map(Double.box).asJava)
              .setXYSeriesRenderStyle(XYSeriesRenderStyle.Scatter).setMarker(SeriesMarkers.CIRCLE)
          }
        chart
      }
    new SwingWrapper(charts.asJava).setTitle(title).displayChartMatrix()
  } else println(s"data for plot $title is empty")
}
