package justin.db

import java.util.UUID

import akka.actor.{Actor, Props}
import justin.db.replication.W

// TODO: assuming that W > 0
class WriteRemoteNodeActor(w: W, storageNodeActors: List[StorageNodeActorRef]) extends Actor {

  val parentStorageNodeActor = context.parent

  var okWrites = 0

  override def receive: Receive = {
    case WriteRemoteNodeActor.SuccessfulWrite if okWrites + 1 == w.w => parentStorageNodeActor ! WriteNodeActor.SuccessfulWrite; context.stop(self)
    case WriteRemoteNodeActor.SuccessfulWrite                        => okWrites += 1
    case WriteRemoteNodeActor.SaveReplicatedValue(id, value)         => ??? // TODO: finish
  }
}

object WriteRemoteNodeActor {

  sealed trait WriteNodeMsg
  case class SaveReplicatedValue(id: UUID, value: String) extends WriteNodeMsg
  private case object SuccessfulWrite extends WriteNodeMsg

  def props(w: W, storageNodeActors: List[StorageNodeActorRef]): Props = {
    Props(new WriteRemoteNodeActor(w, storageNodeActors))
  }
}
