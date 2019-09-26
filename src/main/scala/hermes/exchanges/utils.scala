package hermes.exchanges

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

object utils {
  case class ApiKey(public: String, secret: String)

  case class Auth(key: String, algorithm: String) {
    val secret = new SecretKeySpec(key.getBytes, algorithm)
    val mac = Mac.getInstance(algorithm)
    mac.init(secret)

    def generateHmac(message: String): String = {
      val hash = mac.doFinal(message.getBytes)
      DatatypeConverter.printHexBinary(hash)
    }
  }

  trait OrderSide {
    def opposite: OrderSide
  }

  object OrderSide {
    object Buy extends OrderSide {
      override def opposite = Sell
      override def toString = "Buy"
    }

    object Sell extends OrderSide {
      override def opposite = Buy
      override def toString = "Sell"
    }

    def apply(side: String): OrderSide = {
      List(Buy, Sell).find(_.toString.toLowerCase == side.toLowerCase)
          .getOrElse(throw new NoSuchElementException(s"OrderSide $side"))
    }
  }
}
