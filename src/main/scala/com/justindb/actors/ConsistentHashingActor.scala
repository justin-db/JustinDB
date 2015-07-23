package com.justindb.actors

import akka.actor.{ Actor, Props }
import akka.routing.FromConfig
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Terminated
import scala.math.Ordering
import com.justindb.{Record, Key, Node, Ring}
import com.justindb.HashApi.Hash
import com.justindb.HashApi

class ConsistentHashingActor extends Actor {

  var ring = new Ring

  override def receive: Receive = {

    case NodeRegistration =>
      context watch sender()
      self ! AddNode(Node(Key.uuid, sender()))

    case AddNode(node) =>
      val nodeHash = HashApi.makeHash(node.key)
      ring = Ring(ring.underlying + ((nodeHash, node)))

    case AddRecord(record) =>
      val recordHash = HashApi.makeHash(record.key)
      val nodeOpt = Ring.getNode(ring, recordHash)

      nodeOpt match {
        case Some(node) => node.underlyingActor forward AddRecordToNode(record)
        case None => sender() ! NodeFailure("There is no Node in system, try again later.")
      }

    case GetRecord(key: Key) =>
      val recordHash = HashApi.makeHash(key)
      val nodeOpt = Ring.getNode(ring, recordHash)

      nodeOpt match {
        case Some(node) => node.underlyingActor forward GetRecordFromNode(key)
        case None => sender() ! NodeFailure("There is no Node in system, try again later.")
      }

    case Terminated(a) => // todo: remove from ring and stop watch, use ActorLogging
      println(s"Actor $a has been terminated")
  }

}

sealed trait ConsistentHashingMsg
case class AddNode(n: Node) extends ConsistentHashingMsg
case object NodeRegistration extends ConsistentHashingMsg
case class AddRecord[T](r: Record[T]) extends ConsistentHashingMsg
case class NodeFailure(reason: String) extends ConsistentHashingMsg
case class GetRecord(k: Key) extends ConsistentHashingMsg

