package hermes.bots

import java.util.concurrent.atomic.AtomicBoolean

import hermes.config.Strategy
import hermes.exchanges.ExchangeClient

object Bot {
  def apply(strategy: Strategy, exchange: ExchangeClient): Bot = strategy match {
    case strategySpread: Strategy.Spread => spread.SpreadBot(strategySpread, exchange)
  }
}

trait Bot {
  protected val shutdownSignal = new AtomicBoolean(false)
  protected val runner = new Thread(() => {
    while (!shutdownSignal.get) onTick()
    onStop()
  })

  def start(): Unit = runner.start()
  def stop(): Unit = shutdownSignal.set(true)

  protected def onTick(): Unit
  protected def onStop(): Unit
}
