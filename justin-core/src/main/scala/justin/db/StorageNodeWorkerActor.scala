package justin.db

import akka.actor.{Actor, ActorRef, Props}
import justin.consistent_hashing.NodeId
import justin.db.StorageNodeActorProtocol.{StorageNodeReadData, StorageNodeWriteData}

import scala.concurrent.ExecutionContext

class StorageNodeWorkerActor(readCoordinator: ReplicaReadCoordinator, writeCoordinator: ReplicaWriteCoordinator) extends Actor {
  import StorageNodeWorkerActorProtocol._

  private implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case rd: ReadData  => readCoordinator.apply(rd.cmd, rd.clusterMembers).foreach(rd.sender ! _)
    case wd: WriteData => writeCoordinator.apply(wd.cmd, wd.clusterMembers).foreach(wd.sender ! _)
  }
}

object StorageNodeWorkerActorProtocol {
  case class WriteData(sender: ActorRef, clusterMembers: ClusterMembers, cmd: StorageNodeWriteData)
  case class ReadData(sender: ActorRef, clusterMembers: ClusterMembers, cmd: StorageNodeReadData)
}

object StorageNodeWorkerActor {

  def props(readCoordinator: ReplicaReadCoordinator, writeCoordinator: ReplicaWriteCoordinator): Props = {
    Props(new StorageNodeWorkerActor(readCoordinator, writeCoordinator))
  }
}
