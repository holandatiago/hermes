package hermes.bots

import java.util.concurrent.atomic.AtomicBoolean

import hermes.exchanges.{ExchangeClient, OrderSide}
import hermes.exchanges.ExchangeModels._

object SpreadBot {
  case class Config(
      mainCurrency: String,
      minimumSpread: BigDecimal,
      minimumVolume: BigDecimal,
      maximumAmount: BigDecimal)
}

case class SpreadBot(config: SpreadBot.Config, exchange: ExchangeClient) {
  val shutdownSignal = new AtomicBoolean(false)
  val runner = new Thread(() => {
    while (!shutdownSignal.get) onTick()
    onStop()
  })

  def start(): Unit = runner.start()
  def stop(): Unit = shutdownSignal.set(true)

  var marketOption: Option[Market] = None

  def onTick(): Unit = marketOption match {
    case None => marketOption = searchForBestMarket
    case Some(market) => tradeOnMarket(market)
  }

  def onStop(): Unit = {
    while (marketOption.isDefined) marketOption.foreach(tradeOnMarket)
  }

  val mainMarkets = exchange.getMarkets
      .filter(_.quoteCurrency == config.mainCurrency)
      .filter(_.active).map(mkt => (mkt.name, mkt)).toMap

  def searchForBestMarket: Option[Market] = {
    exchange.getTickers
        .filter(ticker => mainMarkets.contains(ticker.market))
        .filter(ticker => ticker.baseVolume >= config.minimumVolume)
        .filter(ticker => ticker.ask / ticker.bid >= config.minimumSpread)
        .sortBy(_.baseVolume).reverse.map(_.market)
        .headOption.map(mainMarkets)
  }

  def tradeOnMarket(market: Market): Unit = {
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
    val timeToExit = spread < config.minimumSpread || shutdownSignal.get
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

  def calculateBestBid(orderBook: OrderBook): BigDecimal = {
    orderBook.buy.head.price
  }

  def calculateBestAsk(orderBook: OrderBook): BigDecimal = {
    orderBook.sell.head.price
  }
}
