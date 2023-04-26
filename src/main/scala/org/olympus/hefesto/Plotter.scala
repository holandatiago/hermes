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
    yMapper: T => Double = null,
    zGrouper: T => Any = (_: T) => "",
    wSplitter: T => Any = (_: T) => "",
    uLines: List[(String, Double => Double)] = Nil,
    vLines: List[(String, Double)] = Nil) {
  def plot(xMapper: T => Double, yMapper: T => Double): Plotter[T] = copy(xMapper = xMapper, yMapper = yMapper)
  def groupBy(zGrouper: T => Any): Plotter[T] = copy(zGrouper = zGrouper)
  def splitBy(wSplitter: T => Any): Plotter[T] = copy(wSplitter = wSplitter)
  def addCurve(uName: String, uFunc: Double => Double): Plotter[T] = copy(uLines = (uName, uFunc) :: uLines)
  def addVertical(vName: String, vValue: Double): Plotter[T] = copy(vLines = (vName, vValue) :: vLines)

  def display(title: String = ""): Unit = if (data.nonEmpty) {
    val (minXValue, maxXValue) = (-1, 1)
    val (minYValue, maxYValue) = (0.3, 1.5)
    val range = Range.BigDecimal(-1.2, 1.2, 0.1).toList.map(_.toDouble)
    val charts = data
      .groupBy(wSplitter)
      .toList.sortBy(_._1.toString)
      .map { case (part, wValues) =>
        val chart = new XYChart(800, 600, ChartTheme.GGPlot2)
        chart.setTitle(part.toString)
        chart.getStyler.setCursorEnabled(true)
        chart.getStyler
          .setXAxisMin(minXValue).setXAxisMax(maxXValue)
          .setYAxisMin(minYValue).setYAxisMax(maxYValue)
        vLines.foreach { case (vName, vValue) =>
          chart.addAnnotation(new AnnotationLine(vValue, true, false))
          chart.addAnnotation(new AnnotationText(vName, vValue, minYValue, false))
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
              .addSeries(group.toString, zValues.map(xMapper).asJava, zValues.map(yMapper).map(Double.box).asJava)
              .setXYSeriesRenderStyle(XYSeriesRenderStyle.Scatter).setMarker(SeriesMarkers.CIRCLE)
          }
        chart
      }
    new SwingWrapper(charts.asJava).setTitle(title).displayChartMatrix()
  } else println(s"data for plot $title is empty")
}
