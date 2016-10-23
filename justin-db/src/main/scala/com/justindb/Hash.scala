package com.justindb

import java.security.MessageDigest

object HashApi {

  type Hash = String

  def makeHash(forKey: Key): Hash = {
    MessageDigest
      .getInstance("MD5")
      .digest(forKey.value.getBytes)
      .map("%02x".format(_))
      .mkString
  }
}