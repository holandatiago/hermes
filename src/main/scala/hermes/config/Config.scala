package hermes.config

import caseapp._
import pureconfig._
import pureconfig.generic.auto._

case class Config(strategy: Strategy, account: Account)

object Config {
  case class ConfigLists(strategies: List[Strategy], accounts: List[Account])
  case class ConfigNames(strategyName: String, accountName: String) {
    def toConfig: Config = Config(Strategy(strategyName), Account(accountName))
  }

  val value: ConfigLists = loadConfigOrThrow[ConfigLists]

  def apply(args: Array[String]): Config = {
    CaseApp.parse[ConfigNames](args) match {
      case Right((parsedArgs, rawArgs)) => parsedArgs.toConfig
      case Left(error) => throw new RuntimeException(error.message)
    }
  }
}
