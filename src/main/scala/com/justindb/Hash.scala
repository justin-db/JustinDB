package com.justindb

import scala.util.hashing.MurmurHash3

object HashApi {
  type Hash = Int

  def makeHash(key: Key): Hash = MurmurHash3.stringHash(key.value)
}