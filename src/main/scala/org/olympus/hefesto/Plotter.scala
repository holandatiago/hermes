package org.olympus.hefesto

import org.knowm.xchart.XYSeries.XYSeriesRenderStyle
import org.knowm.xchart._
import org.knowm.xchart.style.Styler.ChartTheme
import org.knowm.xchart.style.markers.SeriesMarkers

import scala.collection.JavaConverters._

object Plotter {
  implicit def iterableToPlotter[T](x: Iterable[T]): Plotter[T] = new Plotter[T](x.toList)
}

class Plotter[T](data: List[T]) {
  private val chart = new XYChartBuilder().width(1600).height(900).theme(ChartTheme.GGPlot2).build()

  def fieldAccessor[U](fieldName: String): T => U = { asset =>
    val field = asset.getClass.getDeclaredField(fieldName)
    field.setAccessible(true)
    field.get(asset).asInstanceOf[U]
  }

  def plotFields(xField: String, yField: String, zField: String = null): this.type = {
    chart.setXAxisTitle(xField)
    chart.setYAxisTitle(yField)
    val zGrouper = if (zField == null) (_: T) => "" else fieldAccessor(zField)
    plot(fieldAccessor[Double](xField), fieldAccessor[Double](yField), zGrouper)
  }

  def plot(xMapper: T => Double, yMapper: T => Double, zGrouper: T => Any = _ => ""): this.type = {
    data
      .groupBy(zGrouper)
      .foreach { case (group, values) =>
        chart
          .addSeries(group.toString, values.map(xMapper).asJava, values.map(yMapper).map(Double.box).asJava)
          .setXYSeriesRenderStyle(XYSeriesRenderStyle.Scatter).setMarker(SeriesMarkers.CIRCLE)
      }
    this
  }

  def addCurve(curveName: String, curveFunc: Double => Double): this.type = {
    val minValue = chart.getSeriesMap.asScala.values.map(_.getXMin).min
    val maxValue = chart.getSeriesMap.asScala.values.map(_.getXMax).max
    val width = maxValue - minValue
    val range = Range.BigDecimal(minValue - width / 10, maxValue + width / 10, width / 100).toList.map(_.doubleValue)
    chart.getStyler.setXAxisMin(minValue)
    chart.getStyler.setXAxisMax(maxValue)

    chart
      .addSeries(curveName, range.asJava, range.map(curveFunc).map(Double.box).asJava)
      .setXYSeriesRenderStyle(XYSeriesRenderStyle.Line).setMarker(SeriesMarkers.NONE)
    this
  }

  def addVertical(name: String, value: Double): this.type = {
    val minYValue = chart.getSeriesMap.asScala.values.map(_.getYMin).min
    chart.addAnnotation(new AnnotationLine(value, true, false))
    chart.addAnnotation(new AnnotationText(name, value, minYValue, false))
    this
  }

  def display(title: String = ""): Unit = {
    chart.setTitle(title)
    chart.getStyler.setCursorEnabled(true)
    new SwingWrapper(chart).displayChart()
  }
}
