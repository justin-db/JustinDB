package com.justindb.actors

import akka.actor.{ Actor, Props }
import akka.routing.FromConfig
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Terminated
import scala.math.Ordering
import com.justindb.{ Record, Key, Node, Ring }
import com.justindb.HashApi.Hash
import com.justindb.HashApi

class ConsistentHashingActor extends Actor {
  import ConsistentHashingActor._
  import NodeActor._

  var ring = new Ring

  override def receive: Receive = {
    case NodeRegistration(key) =>
      context watch sender()
      self ! AddNode(Node(key, sender()))

    case AddNode(node) =>
      val nodeHash = HashApi.makeHash(forKey = node.key)
      val newRing = Ring.addNode(ring, nodeHash, node)
      ring = newRing

    case AddRecord(key, record) =>
      val recordHash = HashApi.makeHash(forKey = key)
      val nodeOpt = Ring.getNode(ring, recordHash)

      nodeOpt match {
        case Some(node) => node.underlyingActor forward AddRecordToNode(key, record)
        case None => sender() ! NodeFailure(s"Ring is empty, try again later.")
      }

    case GetRecord(key: Key) =>
      val recordHash = HashApi.makeHash(forKey = key)
      val nodeOpt = Ring.getNode(ring, recordHash)

      nodeOpt match {
        case Some(node) => node.underlyingActor forward GetRecordFromNode(key)
        case None => sender() ! NodeFailure(s"Ring is empty, try again later.")
      }

    case GetNodesKey => sender() ! NodesKeys(ring.nodesKey)

    case Terminated(actor) => // todo: what need to be done in case of Node failure
      println(s"Actor $actor has been terminated")
  }

}

object ConsistentHashingActor {
  sealed trait ConsistentHashingMsg
  case class AddNode(n: Node) extends ConsistentHashingMsg
  case class NodeRegistration(forKey: Key) extends ConsistentHashingMsg
  case class AddRecord[T](forKey: Key, r: Record[T]) extends ConsistentHashingMsg
  case class NodeFailure(reason: String) extends ConsistentHashingMsg
  case class GetRecord(forKey: Key) extends ConsistentHashingMsg
  case object GetNodesKey extends ConsistentHashingMsg
  case class NodesKeys(keys: Iterable[Key]) extends ConsistentHashingMsg
}
