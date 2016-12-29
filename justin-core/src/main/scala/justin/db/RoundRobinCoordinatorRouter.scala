package justin.db

import akka.actor.Props
import akka.routing.{DefaultResizer, RoundRobinPool}
import justin.consistent_hashing.{NodeId, Ring}
import justin.db.replication.N
import justin.db.storage.PluggableStorageProtocol

import scala.concurrent.ExecutionContext

object RoundRobinCoordinatorRouter {
  def routerName: String = "CoordinatorRouter"

  def props(nodeId: NodeId, ring: Ring, n: N, storage: PluggableStorageProtocol)(implicit ec: ExecutionContext): Props = {
    val readCoordinator  = new ReplicaReadCoordinator(nodeId, ring, n, new ReplicaLocalReader(storage), new ReplicaRemoteReader)
    val writeCoordinator = new ReplicaWriteCoordinator(nodeId, ring, n, new ReplicaLocalWriter(storage), new ReplicaRemoteWriter)
    val actorProps = StorageNodeWorkerActor.props(readCoordinator, writeCoordinator)

    RoundRobinPool(nrOfInstances = 5, resizer = Some(DefaultResizer(lowerBound = 2, upperBound = 15))).props(actorProps)
  }
}
