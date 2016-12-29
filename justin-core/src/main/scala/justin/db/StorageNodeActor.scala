package justin.db

import akka.actor.{Actor, ActorRef, Props, RootActorPath}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.{Cluster, Member, MemberStatus}
import akka.routing.{DefaultResizer, RoundRobinPool}
import justin.consistent_hashing.{NodeId, Ring}
import justin.db.StorageNodeActorProtocol._
import justin.db.replication.N
import justin.db.storage.PluggableStorageProtocol

import scala.concurrent.ExecutionContext

class StorageNodeActor(nodeId: NodeId, storage: PluggableStorageProtocol, ring: Ring, n: N) extends Actor with ClusterSubscriberActor {

  private implicit val ec: ExecutionContext = context.dispatcher

  private val coorinatorRouter = context.actorOf(
    props = RoundRobinCoordinatorRouter.props(nodeId, ring, n, storage),
    name  = RoundRobinCoordinatorRouter.routerName
  )

  def receive: Receive = receiveDataPF orElse receiveClusterPF(nodeId, ring) orElse notHandledPF

  private def receiveDataPF: Receive = {
    case readData: StorageNodeReadData   => coorinatorRouter ! StorageNodeWorkerActorProtocol.ReadData(sender(), clusterMembers, readData)
    case writeData: StorageNodeWriteData => coorinatorRouter ! StorageNodeWorkerActorProtocol.WriteData(sender(), clusterMembers, writeData)
  }

  private def notHandledPF: Receive = {
    case t => println("[StorageNodeActor] not handled msg: " + t)
  }
}

object StorageNodeActor {

  def role: String = "StorageNode"

  def name(nodeId: NodeId): String = s"id-${nodeId.id}"

  def props(nodeId: NodeId, storage: PluggableStorageProtocol, ring: Ring, n: N): Props = {
    Props(new StorageNodeActor(nodeId, storage, ring, n))
  }
}
