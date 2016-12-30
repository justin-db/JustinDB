package justin.db

import akka.actor.{Actor, Props}
import justin.consistent_hashing.{NodeId, Ring}
import justin.db.StorageNodeActorProtocol._
import justin.db.replication.N
import justin.db.storage.PluggableStorageProtocol

class StorageNodeActor(nodeId: NodeId, storage: PluggableStorageProtocol, ring: Ring, n: N) extends Actor with ClusterSubscriberActor {

  private val coordinatorRouter = context.actorOf(
    props = RoundRobinCoordinatorRouter.props(nodeId, ring, n, storage)(context.dispatcher),
    name  = RoundRobinCoordinatorRouter.routerName
  )

  def receive: Receive = receiveDataPF orElse receiveClusterPF(nodeId, ring) orElse notHandledPF

  private def receiveDataPF: Receive = {
    case readData: StorageNodeReadData   => coordinatorRouter ! ReplicaCoordinatorActorProtocol.ReadData(sender(), clusterMembers, readData)
    case writeData: StorageNodeWriteData => coordinatorRouter ! ReplicaCoordinatorActorProtocol.WriteData(sender(), clusterMembers, writeData)
  }

  private def notHandledPF: Receive = {
    case t => println("[StorageNodeActor] not handled msg: " + t)
  }
}

object StorageNodeActor {
  def role: String = "StorageNode"
  def name(nodeId: NodeId): String = s"id-${nodeId.id}"
  def props(nodeId: NodeId, storage: PluggableStorageProtocol, ring: Ring, n: N): Props = Props(new StorageNodeActor(nodeId, storage, ring, n))
}
