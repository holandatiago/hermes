package hermes.exchanges

import pureconfig._
import pureconfig.generic.auto._

object Account {
  case class Accounts(accounts: List[Account])

  val values: List[Account] = loadConfigOrThrow[Accounts].accounts
  def test: List[Account] = values.filter(_.test)

  def apply(name: String): Account = {
    values.find(_.name.toLowerCase == name.toLowerCase)
        .getOrElse(throw new NoSuchElementException(s"Account $name"))
  }
}

case class Account(
    name: String,
    exchange: String,
    publicKey: String,
    privateKey: String,
    test: Boolean)
