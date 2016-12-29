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

  private val workerRouter = context.actorOf(
    props = StorageNodeActor.WorkerRouter.props(nodeId, ring, n, storage),
    name  = StorageNodeActor.WorkerRouter.routerName
  )

  def receive: Receive = receiveDataPF orElse receiveClusterPF(nodeId, ring) orElse notHandledPF

  private def receiveDataPF: Receive = {
    case readData: StorageNodeReadData   => workerRouter ! StorageNodeWorkerActorProtocol.ReadData(sender(), clusterMembers, readData)
    case writeData: StorageNodeWriteData => workerRouter ! StorageNodeWorkerActorProtocol.WriteData(sender(), clusterMembers, writeData)
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

  object WorkerRouter {
    def routerName: String = "WorkerRouter"

    def props(nodeId: NodeId, ring: Ring, n: N, storage: PluggableStorageProtocol)(implicit ec: ExecutionContext): Props = {
      val readCoordinator  = new ReplicaReadCoordinator(nodeId, ring, n, new ReplicaLocalReader(storage), new ReplicaRemoteReader)
      val writeCoordinator = new ReplicaWriteCoordinator(nodeId, ring, n, new ReplicaLocalWriter(storage), new ReplicaRemoteWriter)
      val actorProps = StorageNodeWorkerActor.props(readCoordinator, writeCoordinator)
      RoundRobinPool(nrOfInstances = 5, resizer = Some(DefaultResizer(lowerBound = 2, upperBound = 15))).props(actorProps)
    }
  }
}

case class StorageNodeActorRef(storageNodeActor: ActorRef) extends AnyVal
