package hermes.utils

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

case class Authenticator(key: String, algorithm: String) {
  val secret = new SecretKeySpec(key.getBytes, algorithm)
  val mac = Mac.getInstance(algorithm)
  mac.init(secret)

  def sign(message: String): String = {
    val hash = mac.doFinal(message.getBytes)
    DatatypeConverter.printHexBinary(hash)
  }
}
