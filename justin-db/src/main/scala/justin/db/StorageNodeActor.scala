package justin.db

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props, RootActorPath}
import akka.cluster.{Cluster, Member, MemberStatus}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import justin.consistent_hashing.{Ring, UUID2PartitionId}
import justin.db.StorageNodeActor.{GetValue, RegisterNode, PutValue, PutReplicatedValue}
import justin.db.replication.{N, PreferenceList}
import justin.db.storage.PluggableStorage

case class StorageNodeActorId(id: Int) extends AnyVal

class StorageNodeActor(nodeId: StorageNodeActorId, storage: PluggableStorage, ring: Ring, replication: N) extends Actor {

  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = receive(Map.empty[StorageNodeActorId, ActorRef])

  private def receive(clusterMembers: Map[StorageNodeActorId, ActorRef]): Receive = {
    case GetValue(id)                      => sender() ! storage.get(id.toString)
    case pv @ PutValue(valueId, data)      => handlePutValue(pv, clusterMembers); sender() ! "ack"
    case PutReplicatedValue(valueId, data) => saveValue(PutValue(valueId, data))
    case state: CurrentClusterState        => state.members.filter(_.status == MemberStatus.Up).foreach(register)
    case RegisterNode(senderNodeId) if notRegistered(senderNodeId, clusterMembers) =>
      val updatedClusterMembers = clusterMembers + (senderNodeId -> sender())
      context.become(receive(updatedClusterMembers))
      sender() ! RegisterNode(nodeId)
    case MemberUp(m)                       => register(m)
    case t                                 => println("not handled msg: " + t)
  }

  private def notRegistered(tryingToRegisterNodeId: StorageNodeActorId, clusterMembers: Map[StorageNodeActorId, ActorRef]) = {
    !clusterMembers.contains(tryingToRegisterNodeId)
  }

  private def saveValue(pv: PutValue) = storage.put(pv.id.toString, pv.value)

  private def handlePutValue(pv: PutValue, clusterMembers: Map[StorageNodeActorId, ActorRef]) = {
    val basePartitionId = new UUID2PartitionId(ring.size).apply(pv.id)
    val uniqueNodesId = (for {
      partitionId <- PreferenceList.apply(basePartitionId, replication, ring.size)
      nodeId      <- ring.getNodeId(partitionId)
    } yield nodeId).distinct

    uniqueNodesId.foreach {
      case selectedNodeId if selectedNodeId.id == nodeId.id => saveValue(pv)
      case selectedNodeId => clusterMembers.get(StorageNodeActorId(selectedNodeId.id)).foreach(_ ! PutReplicatedValue(pv.id, pv.value))
    }
  }

  private def register(member: Member) = {
    val name = if(nodeId.id == 1) "id-0" else "id-1" // TODO: should send to every logic NodeId
    context.actorSelection(RootActorPath(member.address) / "user" / s"$name") ! RegisterNode(nodeId)
  }
}

object StorageNodeActor {

  sealed trait StorageNodeReq
  case class GetValue(id: UUID) extends StorageNodeReq
  case class PutValue(id: UUID, value: String) extends StorageNodeReq
  case class PutReplicatedValue(id: UUID, value: String) extends StorageNodeReq
  case class RegisterNode(nodeId: StorageNodeActorId) extends StorageNodeReq

  def role: String = "StorageNode"

  def name(nodeId: StorageNodeActorId): String = s"id-${nodeId.id}"

  def props(nodeId: StorageNodeActorId, storage: PluggableStorage, ring: Ring, replication: N): Props = {
    Props(new StorageNodeActor(nodeId, storage, ring, replication))
  }
}
