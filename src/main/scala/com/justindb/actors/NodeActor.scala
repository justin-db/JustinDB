package com.justindb.actors

import akka.actor.{ Actor, ActorRef, Props }
import akka.routing.FromConfig
import com.justindb.{Record, Key}
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
    case AddRecordToNode(record) => store = store + (record.key -> record.v)
    case GetRecordFromNode(key) => sender() ! store(key)
    case state: CurrentClusterState => state.members.filter(_.status == MemberStatus.Up) foreach register
    case MemberUp(m) => register(m)
  }

  def register(member: Member): Unit = {
    if(member.hasRole("ringrole")) {
      context.actorSelection(RootActorPath(member.address) / "user" / "ringrole") ! ConsistentHashingActor.NodeRegistration(Key.uuid)
    }
  }
}

object NodeActor {
  sealed trait NodeActorMsg
  case class AddRecordToNode[T](r: Record[T]) extends NodeActorMsg
  case class GetRecordFromNode(k: Key) extends NodeActorMsg
}
