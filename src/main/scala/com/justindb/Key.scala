package com.justindb

case class Key(value: String)

object Key {
  import java.util.UUID.randomUUID

  def uuid = Key(randomUUID.toString)
}