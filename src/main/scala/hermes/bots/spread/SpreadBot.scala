package hermes.bots.spread

import hermes.bots.Bot
import hermes.config._
import hermes.enums.OrderSide
import hermes.exchanges.ExchangeClient
import hermes.exchanges.ExchangeModels._

case class SpreadBot(strategy: Strategy.Spread) extends Bot {
  val exchange: ExchangeClient = ExchangeClient(Account(strategy.account))
  protected var marketOption: Option[Market] = None

  protected def onTick(): Unit = marketOption match {
    case None => marketOption = searchForBestMarket
    case Some(market) => tradeOnMarket(market)
  }

  protected def onStop(): Unit = marketOption match {
    case None => botStatus.set(0)
    case Some(market) => tradeOnMarket(market)
  }

  protected val mainMarkets = exchange.getMarkets
      .filter(_.quoteCurrency == strategy.mainCurrency)
      .filter(_.active).map(mkt => (mkt.name, mkt)).toMap

  protected def searchForBestMarket: Option[Market] = {
    exchange.getTickers
        .filter(ticker => mainMarkets.contains(ticker.market))
        .filter(ticker => ticker.baseVolume >= strategy.minimumVolume)
        .filter(ticker => ticker.ask / ticker.bid >= strategy.minimumSpread)
        .sortBy(_.baseVolume).reverse.map(_.market)
        .headOption.map(mainMarkets)
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

    val bestBid = calculateBestBid(orderBook)
    val bestAsk = calculateBestBid(orderBook)
    val spread = bestAsk / bestBid
    val timeToExit = spread < strategy.minimumSpread || botStatus.get == 2
    if (timeToExit && openOrders.isEmpty && baseBalance < market.minBaseVolume) marketOption = None

    buyOrder match {
      case Some(order) => if (bestBid != order.price || timeToExit) exchange.cancelOrder(order.id)
      case None => if (!timeToExit) exchange.tryToSendOrder(market, OrderSide.Buy, bestBid, quoteBalance / bestBid)
    }

    sellOrder match {
      case Some(order) => if (bestAsk != order.price) exchange.cancelOrder(order.id)
      case None => exchange.tryToSendOrder(market, OrderSide.Sell, bestAsk, baseBalance)
    }
  }

  protected def calculateBestBid(orderBook: OrderBook): BigDecimal = {
    orderBook.buy.head.price
  }

  protected def calculateBestAsk(orderBook: OrderBook): BigDecimal = {
    orderBook.sell.head.price
  }
}
