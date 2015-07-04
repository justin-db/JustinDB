package com.github.justindb.common

trait Record[T] {
  def v: T
}

case class TextRecord(override val v: String) extends Record[String]
case class LongRecord(override val v: Long) extends Record[Long]
