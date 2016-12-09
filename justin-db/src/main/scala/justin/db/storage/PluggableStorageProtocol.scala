package justin.db.storage

import java.util.UUID

import justin.db.Data
import justin.db.storage.PluggableStorageProtocol.{StorageGetData, StoragePutData}

import scala.concurrent.Future

trait PluggableStorageProtocol {
  def get(id: UUID): Future[StorageGetData]
  def put(cmd: StoragePutData): Future[Unit] // TODO: Make Future[Ack]
}

object PluggableStorageProtocol {

  sealed trait StorageGetData
  object StorageGetData {
    case class Single(data: Data)                   extends StorageGetData
    case class Conflicted(data1: Data, data2: Data) extends StorageGetData
    case object None                                extends StorageGetData
  }

  sealed trait StoragePutData
  object StoragePutData {
    case class Single(data: Data)                           extends StoragePutData
    case class Conflict(id: UUID, data1: Data, data2: Data) extends StoragePutData
  }
}
