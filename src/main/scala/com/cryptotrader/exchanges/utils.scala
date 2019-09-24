package com.cryptotrader.exchanges

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

object utils {
  case class ApiKey(key: String, secret: String)

  case class Auth(key: String, algorithm: String) {
    val secret = new SecretKeySpec(key.getBytes, algorithm)
    val mac = Mac.getInstance(algorithm)
    mac.init(secret)

    def generateHmac(message: String): String = {
      val hash = mac.doFinal(message.getBytes)
      DatatypeConverter.printHexBinary(hash)
    }
  }
}
