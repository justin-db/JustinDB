package com.justindb.actors

import akka.actor.{ Actor, ActorRef, Props }
import akka.routing.FromConfig
import com.justindb.{Record, Key}

class NodeActor extends Actor {

  var store: Map[Key, Any] = Map.empty[Key, Any]

  override def receive: Receive = {
    case AddRecordToNode(record) => store = store + (record.key -> record.v)
    case GetRecordFromNode(key) => sender() ! store(key)
  }
}


sealed trait NodeActorMsg
case class AddRecordToNode[T](r: Record[T]) extends NodeActorMsg
case class GetRecordFromNode[T](k: Key) extends NodeActorMsg