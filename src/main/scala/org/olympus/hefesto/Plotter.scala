package org.olympus.hefesto

import org.knowm.xchart.XYSeries.XYSeriesRenderStyle
import org.knowm.xchart.style.Styler.ChartTheme
import org.knowm.xchart._

import scala.collection.JavaConverters._

object Plotter {
  implicit def iterableToPlotter[T](x: Iterable[T]): Plotter[T] = new Plotter[T](x.toList)
}

class Plotter[T](data: List[T]) {
  private val chart = new XYChartBuilder().width(1600).height(900).theme(ChartTheme.GGPlot2).build()

  def plot(xMapper: T => Double, yMapper: T => Double, zGrouper: T => Any = _ => ""): this.type = {
    data
      .groupBy(zGrouper)
      .foreach { case (group, values) =>
        chart
          .addSeries(group.toString, values.map(xMapper).asJava, values.map(yMapper).map(Double.box).asJava)
          .setXYSeriesRenderStyle(XYSeriesRenderStyle.Scatter)
      }
    this
  }

  def display(title: String = ""): Unit = {
    chart.setTitle(title)
    new SwingWrapper(chart).displayChart(title)
  }
}
