package com.justindb

import scala.util.hashing.MurmurHash3

case class Hash(value: Int) extends AnyVal with Ordered[Hash] {
  def compare(that: Hash) = that.value
}

object Hash {
  // todo: change to MD5
  def makeHash(key: Key): Hash = Hash(MurmurHash3.stringHash(key.value))
}