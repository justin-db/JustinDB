package com.justindb.actors

import akka.actor.{ Actor, ActorRef, Props }
import akka.routing.FromConfig
import com.justindb.{ Record, Key }
import akka.actor.RootActorPath
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.Member
import akka.cluster.MemberStatus

class NodeActor extends Actor {
  import NodeActor._

  // todo: make it as Map[Key, Record]
  var store: Map[Key, Any] = Map.empty[Key, Any]

  val cluster = Cluster(context.system)
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case AddRecordToNode(key, record) => store = store + (key -> record.v)
    case GetRecordFromNode(key) => sender() ! store(key) // todo: potentially dangerous operation - use more safety API
    case state: CurrentClusterState => state.members.filter(_.status == MemberStatus.Up) foreach register
    case MemberUp(m) => register(m)
  }

  def register(member: Member): Unit = {
    if (member.hasRole("ringrole")) {
      val actor = context.actorSelection(RootActorPath(member.address) / "user" / "ringrole")
      actor ! ConsistentHashingActor.NodeRegistration(Key.uuid)
    }
  }
}

object NodeActor {
  sealed trait NodeActorMsg
  case class AddRecordToNode[T](forKey: Key, r: Record[T]) extends NodeActorMsg
  case class GetRecordFromNode(forKey: Key) extends NodeActorMsg
}
