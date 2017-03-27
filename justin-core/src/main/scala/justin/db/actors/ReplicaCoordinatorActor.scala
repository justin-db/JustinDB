package justin.db.actors

import akka.actor.{Actor, ActorRef, Props}
import justin.db.actors.StorageNodeActorProtocol.{StorageNodeReadData, StorageNodeWriteData}
import justin.db.{ClusterMembers, ReplicaReadCoordinator, ReplicaWriteCoordinator}

import scala.concurrent.ExecutionContext

class ReplicaCoordinatorActor(readCoordinator: ReplicaReadCoordinator, writeCoordinator: ReplicaWriteCoordinator) extends Actor {
  import ReplicaCoordinatorActorProtocol._

  private implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case rd: ReadData  => readCoordinator.apply(rd.cmd, rd.clusterMembers).foreach(rd.sender ! _)
    case wd: WriteData => writeCoordinator.apply(wd.cmd, wd.clusterMembers).foreach(wd.sender ! _)
  }
}

object ReplicaCoordinatorActorProtocol {
  case class WriteData(sender: ActorRef, clusterMembers: ClusterMembers, cmd: StorageNodeWriteData)
  case class ReadData(sender: ActorRef, clusterMembers: ClusterMembers, cmd: StorageNodeReadData)
}

object ReplicaCoordinatorActor {

  def props(readCoordinator: ReplicaReadCoordinator, writeCoordinator: ReplicaWriteCoordinator): Props = {
    Props(new ReplicaCoordinatorActor(readCoordinator, writeCoordinator))
  }
}
