package com.justindb

import java.security.MessageDigest

object HashApi {

  type Hash = String

  def makeHash(key: Key): Hash = {
    MessageDigest
      .getInstance("MD5")
      .digest(key.value.getBytes)
      .map("%02x".format(_))
      .mkString
  }
}