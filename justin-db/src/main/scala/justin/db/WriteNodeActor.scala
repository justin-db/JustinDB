package justin.db

import java.util.UUID

import akka.actor.{Actor, Props}
import justin.db.StorageNodeActor.PutReplicatedValue
import justin.db.replication.W

// TODO: assuming that W > 0
class WriteNodeActor(w: W, storageNodeActors: List[StorageNodeActorRef]) extends Actor {

  val parentStorageNodeActor = context.parent

  var okWrites = 0

  override def receive: Receive = {
    case WriteNodeActor.SuccessfulWrite if okWrites+1 == w.w => parentStorageNodeActor ! StorageNodeActor.SuccessfulWrite
    case WriteNodeActor.SuccessfulWrite                      => okWrites += 1
    case WriteNodeActor.PropagateData(id, value)             => storageNodeActors.foreach(_.storageNodeActor ! PutReplicatedValue(id, value))
  }
}

object WriteNodeActor {

  sealed trait WriteNodeMsg
  case class PropagateData(id: UUID, value: String) extends WriteNodeMsg
  case object SuccessfulWrite extends WriteNodeMsg

  def props(w: W, storageNodeActors: List[StorageNodeActorRef]): Props = {
    Props(new WriteNodeActor(w, storageNodeActors))
  }
}
