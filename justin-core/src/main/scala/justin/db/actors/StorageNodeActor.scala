package justin.db.actors

import akka.actor.{Actor, ActorRef, Props, RootActorPath, Terminated}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.{Cluster, Member, MemberStatus}
import justin.consistent_hashing.{NodeId, Ring}
import justin.db.actors.protocol.{RegisterNode, _}
import justin.db.cluster.ClusterMembers
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

  def receive: Receive = receiveDataPF orElse receiveClusterDataPF(nodeId, ring) orElse notHandledPF

  private def receiveDataPF: Receive = {
    case readData: StorageNodeReadRequest   => coordinatorRouter ! ReadData(sender(), clusterMembers, readData)
    case writeData: StorageNodeWriteRequest => coordinatorRouter ! WriteData(sender(), clusterMembers, writeData)
  }

  private def notHandledPF: Receive = {
    case t => println("[StorageNodeActor] not handled msg: " + t)
  }

  private def receiveClusterDataPF(nodeId: NodeId, ring: Ring): Receive = {
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
        siblingNodeId <- ring.nodesId.filterNot(_ == nodeId)
        nodeName       = StorageNodeActor.name(siblingNodeId)
        nodeRef        = context.actorSelection(RootActorPath(member.address) / "user" / nodeName)
      } yield nodeRef ! RegisterNode(nodeId)
    }
  }
}

object StorageNodeActor {
  def role: String = "storagenode"
  def name(nodeId: NodeId): String = s"id-${nodeId.id}"
  def props(nodeId: NodeId, storage: PluggableStorageProtocol, ring: Ring, n: N): Props = Props(new StorageNodeActor(nodeId, storage, ring, n))
}

case class StorageNodeActorRef(storageNodeActor: ActorRef) extends AnyVal
