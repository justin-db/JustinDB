package justin.db.storage

import java.util.UUID

import justin.db.Data

import scala.concurrent.Future

sealed trait StorageGetData
object StorageGetData {
  case class Single(data: Data)                   extends StorageGetData
  case class Conflicted(data1: Data, data2: Data) extends StorageGetData
  case object None                                extends StorageGetData
}

trait PluggableStorageProtocol {
  def get(id: UUID): Future[StorageGetData]
  def put(data: Data): Future[Unit]
}
