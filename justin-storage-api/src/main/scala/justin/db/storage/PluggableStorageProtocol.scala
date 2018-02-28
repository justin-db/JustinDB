package justin.db.storage

import java.util.UUID

import justin.db.storage.PluggableStorageProtocol.{Ack, StorageGetData}

import scala.concurrent.Future

trait GetStorageProtocol {
  def get(id: UUID): Future[StorageGetData]
}

trait PutStorageProtocol {
  def put(data: JustinData): Future[Ack]
}

trait PluggableStorageProtocol extends GetStorageProtocol with PutStorageProtocol

object PluggableStorageProtocol {
  
  sealed trait StorageGetData
  object StorageGetData {
    case class Single(data: JustinData) extends StorageGetData
    case object None                    extends StorageGetData
  }

  sealed trait Ack
  case object Ack extends Ack {
    val future: Future[Ack] = Future.successful(Ack)
  }
}
