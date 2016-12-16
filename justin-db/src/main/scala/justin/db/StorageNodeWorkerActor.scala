package justin.db

import akka.actor.{Actor, ActorRef, Props}
import justin.consistent_hashing.{NodeId, Ring}
import justin.db.StorageNodeActorProtocol.{StorageNodeReadData, StorageNodeReadingResult, StorageNodeWriteData, StorageNodeWritingResult}
import justin.db.replication.N
import justin.db.storage.PluggableStorageProtocol

import scala.concurrent.ExecutionContext

class StorageNodeWorkerActor(nodeId: NodeId, storage: PluggableStorageProtocol, ring: Ring, n: N) extends Actor {
  import StorageNodeWorkerActorProtocol._

  private implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case rd: ReadData  => readData(rd.sender, rd.clusterMembers, rd.cmd)
    case wd: WriteData => writeData(wd.sender, wd.clusterMembers, wd.cmd)
  }

  private def readData(sender: ActorRef, clusterMembers: ClusterMembers, readCmd: StorageNodeReadData) = {
    def sendBack(msg: StorageNodeReadingResult) = sender ! msg

    new ReplicaReadCoordinator(nodeId, clusterMembers, ring, n, new ReplicaLocalReader(storage), new RemoteDataReader)
      .apply(readCmd)
      .foreach(sendBack)
  }

  private def writeData(sender: ActorRef, clusterMembers: ClusterMembers, writeCmd: StorageNodeWriteData) = {
    def sendBack(msg: StorageNodeWritingResult) = sender ! msg

    new ReplicaWriteCoordinator(nodeId, clusterMembers, ring, n, new ReplicaLocalWriter(storage), new RemoteDataWriter)
      .apply(writeCmd)
      .foreach(sendBack)
  }
}

object StorageNodeWorkerActorProtocol {

  case class WriteData(sender: ActorRef, clusterMembers: ClusterMembers, cmd: StorageNodeWriteData)
  case class ReadData(sender: ActorRef, clusterMembers: ClusterMembers, cmd: StorageNodeReadData)
}

object StorageNodeWorkerActor {

  def props(nodeId: NodeId, storage: PluggableStorageProtocol, ring: Ring, n: N): Props = {
    Props(new StorageNodeWorkerActor(nodeId, storage, ring, n))
  }
}
