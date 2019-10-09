package hermes.config

case class Account(
    name: String,
    exchange: String,
    publicKey: String,
    privateKey: String,
    test: Boolean)

object Account {
  val values: List[Account] = Config.value.accounts

  def apply(name: String): Account = {
    values.find(_.name.toLowerCase == name.toLowerCase)
        .getOrElse(throw new NoSuchElementException(s"Account $name"))
  }
}
