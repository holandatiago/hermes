package org.olympus.hermes.bots.spread

import org.olympus.hermes.bots.Bot
import org.olympus.hermes.config._
import org.olympus.hermes.enums.OrderSide
import org.olympus.hermes.exchanges.ExchangeClient
import org.olympus.hermes.exchanges.ExchangeModels._

case class SpreadBot(strategy: Strategy.Spread) extends Bot {
  val exchange: ExchangeClient = ExchangeClient(Account(strategy.account), strategy.rateLimit)
  protected var marketOption: Option[Market] = None

  def setMarket(marketName: String): Unit = {
    marketOption = Some(mainMarkets(marketName))
  }

  protected def onTick(): Unit = marketOption match {
    case None => searchForBestMarket()
    case Some(market) => tradeOnMarket(market)
  }

  protected def onStop(): Unit = marketOption match {
    case None => botStatus.set(0)
    case Some(market) => tradeOnMarket(market)
  }

  protected val mainMarkets = exchange.getMarkets
      .filter(_.quoteCurrency == strategy.mainCurrency)
      .filter(_.active).map(mkt => (mkt.name, mkt)).toMap

  protected def searchForBestMarket(): Unit = {
    val tickers = exchange.getTickers
        .filter(ticker => mainMarkets.contains(ticker.market))
        .filter(ticker => ticker.quoteVolume >= strategy.minimumVolume)
        .filter(ticker => ticker.ask / ticker.bid >= strategy.minimumSpread)
        .filter(ticker => ticker.ask - ticker.bid >= strategy.minimumTicks * mainMarkets(ticker.market).tickPrice)
        .sortBy(ticker => if (strategy.orderBySpread) ticker.ask / ticker.bid else ticker.quoteVolume).reverse
    marketOption = tickers.map(_.market).headOption.map(mainMarkets)
    marketOption match {
      case None =>
        println("There are no markets satisfying the minimum conditions.")
        Thread.sleep(1000)
      case Some(market) =>
        val ticker = tickers.head
        println(s"Entering on market ${market.name}, with spread ${ticker.ask / ticker.bid} and volume ${ticker.baseVolume}")
    }
  }

  protected def tradeOnMarket(market: Market): Unit = {
    val balances = exchange.getBalances
    val orderBook = exchange.getOrderBook(market.name)
    val openOrders = exchange.getOpenOrders(market.name)

    val buyOrder = openOrders.find(_.side == OrderSide.Buy)
    val sellOrder = openOrders.find(_.side == OrderSide.Sell)

    val baseBalance = balances
        .find(_.currency == market.baseCurrency)
        .map(_.available).getOrElse(BigDecimal(0))
    val quoteBalance = balances
        .find(_.currency == market.quoteCurrency)
        .map(_.available).getOrElse(BigDecimal(0))

    val bestBid = orderBook.buy.head.price + market.tickPrice
    val bestAsk = orderBook.sell.head.price - market.tickPrice

    var bidWindow = buyOrder.map(_.remainingVolume).getOrElse(BigDecimal(0)) + (strategy.volumeWindow / bestBid)
    val worstBid = orderBook.buy
        .find { page => bidWindow -= page.volume; bidWindow < 0 }
        .getOrElse(orderBook.buy.last).price + market.tickPrice
    var askWindow = sellOrder.map(_.remainingVolume).getOrElse(BigDecimal(0)) + (strategy.volumeWindow / bestAsk)
    val worstAsk = orderBook.sell
        .find { page => askWindow -= page.volume; askWindow < 0 }
        .getOrElse(orderBook.sell.last).price - market.tickPrice

    val spread = orderBook.sell.head.price / orderBook.buy.head.price
    val timeToExit = spread < strategy.minimumSpread || botStatus.get == 2
    if (timeToExit && openOrders.isEmpty && baseBalance < market.minBaseVolume) marketOption = None
    val amountToTrade = quoteBalance min strategy.maximumAmount min (strategy.maximumBought - baseBalance * bestBid)

    println(s"On market ${market.name}:")
    println(s"    ${market.quoteCurrency} balance: $quoteBalance, ${market.baseCurrency} balance: $baseBalance")
    println(s"    Spread: $spread")
    println(s"    BestBid: $bestBid, WorstBid: $worstBid")
    println(s"    BestAsk: $bestAsk, WorstAsk: $worstAsk")
    buyOrder.foreach(order => println(s"    Active Buy Order. (price: ${order.price}, volume: ${order.volume}, remaining: ${order.remainingVolume})"))
    sellOrder.foreach(order => println(s"    Active Sell Order. (price: ${order.price}, volume: ${order.volume}, remaining: ${order.remainingVolume})"))

    buyOrder match {
      case Some(order) => if (order.price < worstBid || timeToExit) {
        exchange.cancelOrder(order.id)
        println(s"        Cancelled open Buy order.")
      }
      case None => if (!timeToExit) exchange.tryToSendOrder(market, OrderSide.Buy, bestBid, amountToTrade / bestBid)
    }

    sellOrder match {
      case Some(order) => if (order.price > worstAsk) {
        exchange.cancelOrder(order.id)
        println(s"        Cancelled open Sell order.")
      }
      case None => exchange.tryToSendOrder(market, OrderSide.Sell, bestAsk, baseBalance)
    }
  }
}
