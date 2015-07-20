package com.justindb.actors

import akka.actor.{ Actor, Props }
import akka.routing.FromConfig
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import scala.collection.immutable.{ TreeMap, SortedMap }
import scala.util.hashing.MurmurHash3
import scala.math.Ordering

case class Key(value: String)

case class Hash(value: Int) extends AnyVal with Ordered[Hash] {
  def compare(that: Hash) = that.value
}

object Hash {
  def makeHash(key: Key): Hash = Hash(MurmurHash3.stringHash(key.value))
}

case class Ring(underlying: SortedMap[Hash, Node] = TreeMap.empty) extends AnyVal

class ConsistentHashingActor extends Actor {

  var ring = new Ring

  override def receive: Receive = {
    case AddNode(node) => {
      val nodeHash = Hash.makeHash(node.key)
      ring = Ring(ring.underlying + ((nodeHash, node)))
    }
  }
}

sealed trait ConsistentHashingMsg
case class AddNode(n: Node) extends ConsistentHashingMsg
