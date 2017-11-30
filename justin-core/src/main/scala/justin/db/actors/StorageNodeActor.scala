package justin.db.actors

import akka.actor.{Actor, ActorRef, Props, RootActorPath, Terminated}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.{Cluster, Member, MemberStatus}
import com.typesafe.scalalogging.StrictLogging
import justin.db.actors.protocol.{RegisterNode, _}
import justin.db.cluster.ClusterMembers
import justin.db.cluster.datacenter.Datacenter
import justin.db.consistenthashing.{NodeId, Ring}
import justin.db.replica._
import justin.db.replica.read.{ReplicaLocalReader, ReplicaReadCoordinator, ReplicaRemoteReader}
import justin.db.replica.write.{ReplicaLocalWriter, ReplicaRemoteWriter, ReplicaWriteCoordinator}
import justin.db.storage.PluggableStorageProtocol

import scala.concurrent.ExecutionContext

class StorageNodeActor(nodeId: NodeId, datacenter: Datacenter, storage: PluggableStorageProtocol, ring: Ring, n: N) extends Actor with StrictLogging {

  private[this] implicit val ec: ExecutionContext = context.dispatcher
  private[this] val cluster = Cluster(context.system)

  private[this] var clusterMembers   = ClusterMembers.empty
  private[this] val readCoordinator  = new ReplicaReadCoordinator(nodeId, ring, n, new ReplicaLocalReader(storage), new ReplicaRemoteReader)
  private[this] val writeCoordinator = new ReplicaWriteCoordinator(nodeId, ring, n, new ReplicaLocalWriter(storage), new ReplicaRemoteWriter)

  private[this] val coordinatorRouter = context.actorOf(
    props = RoundRobinCoordinatorRouter.props(readCoordinator, writeCoordinator),
    name  = RoundRobinCoordinatorRouter.routerName
  )

  private[this] val name = self.path.name

  override def preStart(): Unit = cluster.subscribe(this.self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(this.self)

  def receive: Receive = {
    receiveDataPF orElse receiveClusterDataPF orElse receiveRegisterNodePR orElse notHandledPF
  }

  private[this] def receiveDataPF: Receive = {
    case readReq: StorageNodeReadRequest              =>
      coordinatorRouter ! ReadData(sender(), clusterMembers, readReq)
    case writeLocalDataReq: StorageNodeWriteDataLocal =>
      coordinatorRouter ! WriteData(sender(), clusterMembers, writeLocalDataReq)
    case writeClientReplicaReq: Internal.WriteReplica =>
      coordinatorRouter ! WriteData(sender(), clusterMembers, writeClientReplicaReq)
  }

  private[this] def receiveClusterDataPF: Receive = {
    case "members"                  => sender() ! clusterMembers
    case MemberUp(member)           => register(nodeId, ring, member)
    case state: CurrentClusterState => state.members.filter(_.status == MemberStatus.Up).foreach(member => register(nodeId, ring, member))
    case Terminated(actorRef)       => clusterMembers = clusterMembers.removeByRef(StorageNodeActorRef(actorRef))
  }

  private[this] def receiveRegisterNodePR: Receive = {
    case RegisterNode(senderNodeId) if clusterMembers.notContains(senderNodeId) =>
      val senderRef = sender()
      context.watch(senderRef)
      clusterMembers = clusterMembers.add(senderNodeId, StorageNodeActorRef(senderRef))
      senderRef ! RegisterNode(nodeId)
      logger.info(s"Actor[$name]: Successfully registered node [id-${senderNodeId.id}]")
    case RegisterNode(senderNodeId) =>
      logger.info(s"Actor[$name]: Node [id-${senderNodeId.id}] is already registered")
  }

  private[this] def register(nodeId: NodeId, ring: Ring, member: Member) = {
    (member.hasRole(StorageNodeActor.role), datacenter.name == member.dataCenter) match {
      case (true, true) => register()
      case (_,   false) => logger.info(s"Actor[$name]: $member doesn't belong to datacenter [${datacenter.name}]")
      case (false,   _) => logger.info(s"Actor[$name]: $member doesn't have [${StorageNodeActor.role}] role (it has roles ${member.roles}")
    }

    def register() = for {
      ringNodeId    <- ring.nodesId
      nodeName       = StorageNodeActor.name(ringNodeId, Datacenter(member.dataCenter))
      nodeRef        = context.actorSelection(RootActorPath(member.address) / "user" / nodeName)
    } yield nodeRef ! RegisterNode(nodeId)
  }

  private[this] def notHandledPF: Receive = {
    case t => logger.warn(s"Actor[$name]: Not handled message [$t]")
  }
}

object StorageNodeActor {
  def role: String = "storagenode"
  def name(nodeId: NodeId, datacenter: Datacenter): String = s"${datacenter.name}-id-${nodeId.id}"
  def props(nodeId: NodeId, datacenter: Datacenter, storage: PluggableStorageProtocol, ring: Ring, n: N): Props = {
    Props(new StorageNodeActor(nodeId, datacenter, storage, ring, n))
  }
}

case class StorageNodeActorRef(ref: ActorRef) extends AnyVal
