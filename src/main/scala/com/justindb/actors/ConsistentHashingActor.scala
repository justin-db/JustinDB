package com.justindb.actors

import akka.actor.{ Actor, Props }
import akka.routing.FromConfig
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import scala.math.Ordering
import com.justindb.{Record, Key, Hash, Node, Ring}

class ConsistentHashingActor extends Actor {

  var ring = new Ring

  override def receive: Receive = {
    case AddNode(node) => {
      val nodeHash = Hash.makeHash(node.key)
      ring = Ring(ring.underlying + ((nodeHash, node)))
    }
    case AddRecord(record) => {
      val recordHash = Hash.makeHash(record.key)
      val nodeOpt = Ring.getNode(ring, recordHash)

      nodeOpt match {
        case Some(node) => node.underlyingActor forward AddRecordToNode(record)
        case None => sender() ! NodeFailure("There is no Node in system, try again later.")
      }
    }
    case GetRecord(key: Key) => {
      val recordHash = Hash.makeHash(key)
      val nodeOpt = Ring.getNode(ring, recordHash)

      nodeOpt match {
        case Some(node) => node.underlyingActor forward GetRecordFromNode(key)
        case None => sender() ! NodeFailure("There is no Node in system, try again later.")
      }
    }
  }
}

sealed trait ConsistentHashingMsg
case class AddNode(n: Node) extends ConsistentHashingMsg
case class AddRecord[T](r: Record[T]) extends ConsistentHashingMsg
case class NodeFailure(reason: String) extends ConsistentHashingMsg
case class GetRecord(k: Key) extends ConsistentHashingMsg