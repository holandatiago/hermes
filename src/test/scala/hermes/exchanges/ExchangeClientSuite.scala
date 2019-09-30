package hermes.exchanges

import org.scalatest.FunSpec

import scala.util.Try

class ExchangeClientSuite extends FunSpec {
  Account.test.map(ExchangeClient.apply).foreach(testClient)

  def testClient(client: ExchangeClient): Unit = describe(client.getClass.getName) {
    val marketsTry = Try(client.getMarkets)
    val mainMarketOption = marketsTry.toOption
        .flatMap(_.filter(_.baseCurrency == "ETH").find(_.quoteCurrency == "BTC"))
    describe("getMarkets") {
      it("should call getMarkets with no errors.") {
        assert(marketsTry.isSuccess)
      }
      it("markets list should not be empty.") {
        assume(marketsTry.isSuccess)
        assert(marketsTry.get.nonEmpty)
      }
      it("ETHBTC market exists and is active.") {
        assume(marketsTry.isSuccess)
        assert(mainMarketOption.isDefined)
        assert(mainMarketOption.get.active)
      }
    }

    val tickersTry = Try(client.getTickers)
    val mainTickerOption = tickersTry.toOption
        .flatMap(_.find(ticker => mainMarketOption.exists(_.name == ticker.market)))
    describe("getTickers") {
      it("should call getTickers with no errors.") {
        assert(tickersTry.isSuccess)
      }
      it("tickers list should not be empty.") {
        assume(tickersTry.isSuccess)
        assert(tickersTry.get.nonEmpty)
      }
      it("ETHBTC ticker exists and is valid.") {
        assume(tickersTry.isSuccess)
        assume(mainMarketOption.isDefined)
        assert(mainTickerOption.isDefined)
        assert(mainTickerOption.get.baseVolume > 0)
      }
    }

    val orderBookTry = Try(client.getOrderBook(mainMarketOption.get.name))
    describe("getOrderBook") {
      it("should call getOrderBook with no errors.") {
        assume(mainMarketOption.isDefined)
        assert(orderBookTry.isSuccess)
      }
      it("order book lists sizes should be 100.") {
        assume(orderBookTry.isSuccess)
        assert(orderBookTry.get.buy.size == 100)
        assert(orderBookTry.get.sell.size == 100)
      }
      it("order book lists should be ordered by price.") {
        assume(orderBookTry.isSuccess)
        assert(orderBookTry.get.buy.sortBy(_.price).reverse == orderBookTry.get.buy)
        assert(orderBookTry.get.sell.sortBy(_.price) == orderBookTry.get.sell)
      }
    }

    val lastTradesTry = Try(client.getLastTrades(mainMarketOption.get.name))
    describe("getLastTrades") {
      it("should call getLastTrades with no errors.") {
        assume(mainMarketOption.isDefined)
        assert(lastTradesTry.isSuccess)
      }
      it("last trades list size should be 100.") {
        assume(lastTradesTry.isSuccess)
        assert(lastTradesTry.get.size == 100)
      }
      it("last trades list should be ordered by timestamp.") {
        assume(lastTradesTry.isSuccess)
        assert(lastTradesTry.get.sortBy(_.timestamp) == lastTradesTry.get)
      }
    }

    val balancesTry = Try(client.getBalances)
    val mainBalanceOption = balancesTry.toOption
        .flatMap(_.find(_.currency == "BTC"))
    describe("getBalances") {
      it("should call getBalances with no errors.") {
        assert(balancesTry.isSuccess)
      }
      it("balances list should not be empty.") {
        assume(balancesTry.isSuccess)
        assert(balancesTry.get.nonEmpty)
      }
      it("BTC balance exists and has enough funds.") {
        assume(balancesTry.isSuccess)
        assert(mainBalanceOption.isDefined)
        assert(mainBalanceOption.get.available >= BigDecimal("0.0005"))
      }
    }

    val openOrdersTry = Try(client.getOpenOrders(mainMarketOption.get.name))
    describe("getOpenOrders") {
      it("should call getOpenOrders with no errors.") {
        assume(mainMarketOption.isDefined)
        assert(openOrdersTry.isSuccess)
      }
      it("open orders list should be empty.") {
        assume(openOrdersTry.isSuccess)
        assert(openOrdersTry.get.isEmpty)
      }
    }

    val sendOrderTry = Try(client.sendOrder(market = mainMarketOption.get.name,
      side = OrderSide.Buy, price = BigDecimal("0.01"), volume = BigDecimal("0.05")))
    val ordersSentTry = Try(client.getOpenOrders(mainMarketOption.get.name))
    val orderSentOption = ordersSentTry.toOption.flatMap(_.headOption)
    describe("sendOrder") {
      it("should call sendOrder with no errors.") {
        assume(mainMarketOption.isDefined)
        assume(mainBalanceOption.isDefined)
        assume(mainBalanceOption.get.available >= BigDecimal("0.0005"))
        assert(sendOrderTry.isSuccess)
      }
      it("should call getOpenOrders with no errors.") {
        assume(mainMarketOption.isDefined)
        assert(ordersSentTry.isSuccess)
      }
      it("open orders list size should be one.") {
        assume(sendOrderTry.isSuccess)
        assume(ordersSentTry.isSuccess)
        assert(ordersSentTry.get.size == 1)
      }
      it("order sent is the expected one.") {
        assume(sendOrderTry.isSuccess)
        assume(ordersSentTry.isSuccess)
        assert(orderSentOption.isDefined)
        assert(orderSentOption.get.id == sendOrderTry.get)
        assert(orderSentOption.get.market == mainMarketOption.get.name)
        assert(orderSentOption.get.side == OrderSide.Buy)
        assert(orderSentOption.get.price == BigDecimal("0.01"))
        assert(orderSentOption.get.volume == BigDecimal("0.05"))
        assert(orderSentOption.get.remainingVolume == BigDecimal("0.05"))
      }
    }

    val cancelOrderTry = Try(client.cancelOrder(orderSentOption.get.id))
    val cancelAgainOrderTry = Try(client.cancelOrder(orderSentOption.get.id))
    val ordersCancelledTry = Try(client.getOpenOrders(mainMarketOption.get.name))
    describe("cancelOrder") {
      it("should call cancelOrder with no errors.") {
        assume(orderSentOption.isDefined)
        assert(cancelOrderTry.isSuccess)
      }
      it("should call getOpenOrders with no errors.") {
        assume(mainMarketOption.isDefined)
        assert(ordersCancelledTry.isSuccess)
      }
      it("open orders list should be empty.") {
        assume(cancelOrderTry.isSuccess)
        assume(ordersCancelledTry.isSuccess)
        assert(ordersCancelledTry.get.isEmpty)
      }
      it("cancel order too late should not break.") {
        assume(cancelOrderTry.isSuccess)
        assert(cancelAgainOrderTry.isSuccess)
      }
    }
  }
}
