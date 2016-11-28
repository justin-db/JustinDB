package justin.db

import akka.actor.{Actor, ActorRef, Props, RootActorPath}
import akka.cluster.{Cluster, Member, MemberStatus}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import justin.db.StorageNodeActorProtocol._
import justin.db.consistent_hashing.{NodeId, Ring}
import justin.db.replication.N
import justin.db.storage.PluggableStorage

import scala.concurrent.ExecutionContext

class StorageNodeActor(nodeId: NodeId, storage: PluggableStorage, ring: Ring, n: N)(implicit ec: ExecutionContext) extends Actor {

  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = receive(ClusterMembers.empty)

  private def receive(clusterMembers: ClusterMembers): Receive = {
    // READ part
    case cmd: StorageNodeReadData.Replicated => readData(sender(), clusterMembers, cmd)
    case cmd: StorageNodeReadData.Local      => readData(sender(), clusterMembers, cmd)

    // WRITE part
    case cmd: StorageNodeWriteData.Replicate => writeData(sender(), clusterMembers, cmd)
    case cmd: StorageNodeWriteData.Local     => writeData(sender(), clusterMembers, cmd)

    // CLUSTER part
    case RegisterNode(senderNodeId) if clusterMembers.notContains(senderNodeId) =>
      val storageActorRef = StorageNodeActorRef(sender())
      context.become(receive(clusterMembers.add(senderNodeId, storageActorRef)))
      sender() ! RegisterNode(nodeId)
    case MemberUp(m)                => register(m)
    case state: CurrentClusterState => state.members.filter(_.status == MemberStatus.Up).foreach(register)

    // NOT HANDLED
    case t                          => println("[StorageNodeActor] not handled msg: " + t)
  }

  private def readData(sender: ActorRef, clusterMembers: ClusterMembers, readCmd: StorageNodeReadData) = {
    def sendBack(msg: StorageNodeReadingResult) = sender ! msg

    new StorageNodeReadService(nodeId, clusterMembers, ring, n, new LocalDataReadingService(storage), new RemoteDataReadingService)
      .apply(readCmd)
      .foreach(sendBack)
  }

  private def writeData(sender: ActorRef, clusterMembers: ClusterMembers, writeCmd: StorageNodeWriteData) = {
    def sendBack(msg: StorageNodeWritingResult) = sender ! msg

    new StorageNodeWriteService(nodeId, clusterMembers, ring, n, storage)
      .apply(writeCmd)
      .foreach(sendBack)
  }

  private def register(member: Member) = {
    val nodesRefs = for {
      siblingNodeId <- ring.nodesId.filterNot(_ == nodeId)
      nodeName       = StorageNodeActor.name(siblingNodeId)
      nodeRef        = context.actorSelection(RootActorPath(member.address) / "user" / nodeName)
    } yield nodeRef

    nodesRefs.foreach(_ ! RegisterNode(nodeId))
  }
}

object StorageNodeActor {

  def role: String = "StorageNode"

  def name(nodeId: NodeId): String = s"id-${nodeId.id}"

  def props(nodeId: NodeId, storage: PluggableStorage, ring: Ring, n: N)(implicit ec: ExecutionContext): Props = {
    Props(new StorageNodeActor(nodeId, storage, ring, n))
  }
}
