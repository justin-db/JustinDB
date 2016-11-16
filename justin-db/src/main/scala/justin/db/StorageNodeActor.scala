package justin.db

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props, RootActorPath}
import akka.cluster.{Cluster, Member, MemberStatus}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import justin.db.consistent_hashing.{NodeId, Ring}
import justin.db.StorageNodeActor.{GetValue, PutValue, RegisterNode}
import justin.db.replication.{N, W}
import justin.db.storage.PluggableStorage

case class StorageNodeActorRef(storageNodeActor: ActorRef) extends AnyVal

class StorageNodeActor(nodeId: NodeId, storage: PluggableStorage, ring: Ring, replication: N) extends Actor {

  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = receive(ClusterMembers.empty)

  private def receive(clusterMembers: ClusterMembers): Receive = {
    // READ part
    case GetValue(id)                      => sender() ! storage.get(id.toString)

    // WRITE part
    case pv: PutValue                      => sender() ! "ack" // TODO: finish

    // CLUSTER part
    case RegisterNode(senderNodeId) if clusterMembers.notContains(senderNodeId) =>
      val storageActorRef = StorageNodeActorRef(sender())
      context.become(receive(clusterMembers.add(senderNodeId, storageActorRef)))
      sender() ! RegisterNode(nodeId)
    case MemberUp(m)                       => register(m)
    case state: CurrentClusterState        => state.members.filter(_.status == MemberStatus.Up).foreach(register)

    // NOT HANDLED
    case t                                 => println("[StorageNodeActor] not handled msg: " + t)
  }

  private def register(member: Member) = {
    val name = if(nodeId.id == 1) "id-0" else "id-1" // TODO: should send to every logic NodeId
    context.actorSelection(RootActorPath(member.address) / "user" / s"$name") ! RegisterNode(nodeId)
  }
}

object StorageNodeActor {

  sealed trait StorageNodeCmd

  // read part
  case class GetValue(id: UUID) extends StorageNodeCmd

  // write part
  case class PutValue(w: W, id: UUID, value: String) extends StorageNodeCmd
  case object SuccessfulWrite extends StorageNodeCmd
  case object UnsuccessfulWrite extends StorageNodeCmd

  // cluster part
  case class RegisterNode(nodeId: NodeId) extends StorageNodeCmd

  def role: String = "StorageNode"

  def name(nodeId: NodeId): String = s"id-${nodeId.id}"

  def props(nodeId: NodeId, storage: PluggableStorage, ring: Ring, replication: N): Props = {
    Props(new StorageNodeActor(nodeId, storage, ring, replication))
  }
}
