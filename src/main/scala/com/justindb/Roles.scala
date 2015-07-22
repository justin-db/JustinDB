package com.justindb

sealed trait Role
case object RingRole extends Role
case object NodeRole extends Role

object Role {
  implicit def toString(r: Role): String = r.toString
}