package hermes.bots

import java.util.concurrent.atomic.AtomicInteger

import com.typesafe.scalalogging.StrictLogging
import hermes.config.Strategy
import hermes.exchanges.ExchangeClient

object Bot {
  def apply(strategy: Strategy): Bot = strategy match {
    case spreadStrategy: Strategy.Spread => spread.SpreadBot(spreadStrategy)
  }
}

trait Bot extends StrictLogging {
  protected val exchange: ExchangeClient
  protected val botStatus = new AtomicInteger(0)
  protected val runner = new Thread(() => {
    botStatus.set(1)
    while (botStatus.get == 1) onTick()
    while (botStatus.get == 2) onStop()
    exchange.shutdown()
    botStatus.set(0)
  })

  def start(): Unit = runner.start()
  def stop(hard: Boolean = false): Unit = {
    botStatus.set(if (hard) 0 else 2)
    runner.join()
  }

  def status(): String = botStatus.get match {
    case 0 => "Offline"
    case 1 => "Running"
    case 2 => "Exiting"
  }

  protected def onTick(): Unit
  protected def onStop(): Unit
}
