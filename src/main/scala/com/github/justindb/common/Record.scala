package com.github.justindb.common

case class Record[T: AcceptableValue](v: T)

trait AcceptableValue[T]

object AcceptableValue {
  implicit object LongOK extends AcceptableValue[Long]
  implicit object StringOK extends AcceptableValue[String]
}
