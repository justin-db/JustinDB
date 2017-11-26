package justin.db.actors

import akka.actor.{Actor, ActorRef, Props, RootActorPath, Terminated}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.{Cluster, Member, MemberStatus}
import justin.db.actors.protocol.{RegisterNode, _}
import justin.db.cluster.ClusterMembers
import justin.db.consistenthashing.{NodeId, Ring}
import justin.db.replica._
import justin.db.replica.read.{ReplicaLocalReader, ReplicaReadCoordinator, ReplicaRemoteReader}
import justin.db.replica.write.{ReplicaLocalWriter, ReplicaRemoteWriter, ReplicaWriteCoordinator}
import justin.db.storage.PluggableStorageProtocol

class StorageNodeActor(nodeId: NodeId, storage: PluggableStorageProtocol, ring: Ring, n: N) extends Actor {

  private implicit val ec = context.dispatcher
  private val cluster = Cluster(context.system)

  private var clusterMembers   = ClusterMembers.empty
  private val readCoordinator  = new ReplicaReadCoordinator(nodeId, ring, n, new ReplicaLocalReader(storage), new ReplicaRemoteReader)
  private val writeCoordinator = new ReplicaWriteCoordinator(nodeId, ring, n, new ReplicaLocalWriter(storage), new ReplicaRemoteWriter)

  private val coordinatorRouter = context.actorOf(
    props = RoundRobinCoordinatorRouter.props(readCoordinator, writeCoordinator),
    name  = RoundRobinCoordinatorRouter.routerName
  )

  override def preStart(): Unit = cluster.subscribe(this.self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(this.self)

  def receive: Receive = {
    receiveDataPF orElse receiveClusterDataPF orElse notHandledPF
  }

  private def receiveDataPF: Receive = {
    case readReq: StorageNodeReadRequest              =>
      coordinatorRouter ! ReadData(sender(), clusterMembers, readReq)
    case writeLocalDataReq: StorageNodeWriteDataLocal =>
      coordinatorRouter ! WriteData(sender(), clusterMembers, writeLocalDataReq)
    case writeClientReplicaReq: Internal.WriteReplica =>
      coordinatorRouter ! WriteData(sender(), clusterMembers, writeClientReplicaReq)
  }

  private def receiveClusterDataPF: Receive = {
    case "members" => sender() ! clusterMembers
    case RegisterNode(senderNodeId) if clusterMembers.notContains(senderNodeId) =>
      val senderRef = sender()
      context.watch(senderRef)
      clusterMembers = clusterMembers.add(senderNodeId, StorageNodeActorRef(senderRef))
      senderRef ! RegisterNode(nodeId)
    case MemberUp(member)           => register(nodeId, ring, member)
    case state: CurrentClusterState => state.members.filter(_.status == MemberStatus.Up).foreach(member => register(nodeId, ring, member))
    case Terminated(actorRef)       => clusterMembers = clusterMembers.removeByRef(StorageNodeActorRef(actorRef))
  }

  private def register(nodeId: NodeId, ring: Ring, member: Member) = {
    if (member.hasRole(StorageNodeActor.role)) {
      for {
        ringNodeId    <- ring.nodesId
        nodeName       = StorageNodeActor.name(ringNodeId, member.dataCenter)
        nodeRef        = context.actorSelection(RootActorPath(member.address) / "user" / nodeName)
      } yield nodeRef ! RegisterNode(nodeId)
    }
  }

  private def notHandledPF: Receive = {
    case t => println("[StorageNodeActor] not handled msg: " + t)
  }
}

object StorageNodeActor {
  def role: String = "storagenode"
  def name(nodeId: NodeId, datacenter: String): String = s"$datacenter-id-${nodeId.id}"
  def props(nodeId: NodeId, storage: PluggableStorageProtocol, ring: Ring, n: N): Props = Props(new StorageNodeActor(nodeId, storage, ring, n))
}

case class StorageNodeActorRef(ref: ActorRef) extends AnyVal
