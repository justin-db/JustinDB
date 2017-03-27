package justin.db.actors

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import justin.consistent_hashing.{NodeId, Ring}
import justin.db._
import justin.db.actors.StorageNodeActorProtocol.{StorageNodeReadData, StorageNodeWriteData}
import justin.db.actors.protocol.{ReadData, WriteData}
import justin.db.replica._
import justin.db.storage.PluggableStorageProtocol

class StorageNodeActor(nodeId: NodeId, storage: PluggableStorageProtocol, ring: Ring, n: N) extends Actor with ClusterSubscriberActor {

  private implicit val ec = context.dispatcher

  private val readCoordinator  = new ReplicaReadCoordinator(nodeId, ring, n, new ReplicaLocalReader(storage), new ReplicaRemoteReader)
  private val writeCoordinator = new ReplicaWriteCoordinator(nodeId, ring, n, new ReplicaLocalWriter(storage), new ReplicaRemoteWriter)

  private val coordinatorRouter = context.actorOf(
    props = RoundRobinCoordinatorRouter.props(readCoordinator, writeCoordinator),
    name  = RoundRobinCoordinatorRouter.routerName
  )

  def receive: Receive = receiveDataPF orElse receiveClusterDataPF(nodeId, ring) orElse notHandledPF

  private def receiveDataPF: Receive = {
    case readData: StorageNodeReadData   => coordinatorRouter ! ReadData(sender(), clusterMembers, readData)
    case writeData: StorageNodeWriteData => coordinatorRouter ! WriteData(sender(), clusterMembers, writeData)
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

case class StorageNodeActorRef(storageNodeActor: ActorRef) extends AnyVal

object StorageNodeActorProtocol {

  // ----- READ PART ----
  // INPUT
  sealed trait StorageNodeReadData
  object StorageNodeReadData {
    case class Local(id: UUID)            extends StorageNodeReadData
    case class Replicated(r: R, id: UUID) extends StorageNodeReadData
  }
  // OUTPUT
  sealed trait StorageNodeReadingResult
  object StorageNodeReadingResult {
    case class Found(data: Data)           extends StorageNodeReadingResult
    case class Conflicts(data: List[Data]) extends StorageNodeReadingResult
    case object NotFound                   extends StorageNodeReadingResult
    case object FailedRead                 extends StorageNodeReadingResult
  }
  // ------

  // ----- WRITE PART ----
  // INPUT
  sealed trait StorageNodeWriteData
  object StorageNodeWriteData {
    case class Local(data: Data)           extends StorageNodeWriteData
    case class Replicate(w: W, data: Data) extends StorageNodeWriteData
  }
  // OUTPUT
  sealed trait StorageNodeWritingResult
  object StorageNodeWritingResult {
    case object SuccessfulWrite                              extends StorageNodeWritingResult
    case object FailedWrite                                  extends StorageNodeWritingResult
    case class ConflictedWrite(oldData: Data, newData: Data) extends StorageNodeWritingResult
  }
  // ------
}
