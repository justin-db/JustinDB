package justin.db

import akka.actor.{Actor, ActorRef, Props, RootActorPath}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.{Cluster, Member, MemberStatus}
import akka.routing.{DefaultResizer, RoundRobinPool}
import justin.consistent_hashing.{NodeId, Ring}
import justin.db.StorageNodeActorProtocol._
import justin.db.replication.N
import justin.db.storage.PluggableStorageProtocol

class StorageNodeActor(nodeId: NodeId, storage: PluggableStorageProtocol, ring: Ring, n: N) extends Actor {

  private val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  private val workerRouter = context.actorOf(
    props = RoundRobinPool(
      nrOfInstances = 5,
      resizer       = Some(DefaultResizer(lowerBound = 2, upperBound = 15))
    ).props(StorageNodeWorkerActor.props(nodeId, storage, ring, n)),
    name = "workerRouter"
  )

  override def receive: Receive = receive(ClusterMembers.empty)

  private def receive(clusterMembers: ClusterMembers): Receive = {
    case readData: StorageNodeReadData   => workerRouter ! StorageNodeWorkerActorProtocol.ReadData(sender(), clusterMembers, readData)
    case writeData: StorageNodeWriteData => workerRouter ! StorageNodeWorkerActorProtocol.WriteData(sender(), clusterMembers, writeData)

    // cluster
    case RegisterNode(senderNodeId) if clusterMembers.notContains(senderNodeId) =>
      context.become(receive(clusterMembers.add(senderNodeId, StorageNodeActorRef(sender()))))
      sender() ! RegisterNode(nodeId)
    case MemberUp(m)                => register(m)
    case state: CurrentClusterState => state.members.filter(_.status == MemberStatus.Up).foreach(register)

    // other
    case t                          => println("[StorageNodeActor] not handled msg: " + t)
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

  def props(nodeId: NodeId, storage: PluggableStorageProtocol, ring: Ring, n: N): Props = {
    Props(new StorageNodeActor(nodeId, storage, ring, n))
  }
}

case class StorageNodeActorRef(storageNodeActor: ActorRef) extends AnyVal
