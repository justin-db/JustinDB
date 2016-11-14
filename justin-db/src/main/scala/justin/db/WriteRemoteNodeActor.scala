package justin.db

import java.util.UUID

import akka.actor.{Actor, Props}
import justin.db.StorageNodeActor.PutReplicatedValue
import justin.db.replication.W

// TODO: assuming that W > 0
class WriteRemoteNodeActor(w: W, storageNodeActors: List[StorageNodeActorRef]) extends Actor {

  val parentStorageNodeActor = context.parent

  var okWrites = 0

  override def receive: Receive = {
    case WriteRemoteNodeActor.SuccessfulWrite if okWrites+1 == w.w =>
      parentStorageNodeActor ! StorageNodeActor.SuccessfulWrite
      context.stop(self)
    case WriteRemoteNodeActor.SuccessfulWrite                      =>
      okWrites += 1
    case WriteRemoteNodeActor.PropagateData(id, value)             =>
      storageNodeActors.foreach(_.storageNodeActor ! PutReplicatedValue(id, value))
  }
}

object WriteRemoteNodeActor {

  sealed trait WriteNodeMsg
  case class PropagateData(id: UUID, value: String) extends WriteNodeMsg
  case object SuccessfulWrite extends WriteNodeMsg

  def props(w: W, storageNodeActors: List[StorageNodeActorRef]): Props = {
    Props(new WriteRemoteNodeActor(w, storageNodeActors))
  }
}
