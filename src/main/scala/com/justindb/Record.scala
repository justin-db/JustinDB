package com.justindb

case class Record[T: AcceptableValue](key: Key, v: T)

trait AcceptableValue[T]

object AcceptableValue {
  implicit object LongOK extends AcceptableValue[Long]
  implicit object StringOK extends AcceptableValue[String]
}