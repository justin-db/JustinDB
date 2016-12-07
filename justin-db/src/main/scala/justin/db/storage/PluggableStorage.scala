package justin.db.storage

import java.util.UUID

import justin.db.Data

import scala.concurrent.Future

sealed trait StorageGetData
object StorageGetData {
  case class Single(data: Data)     extends StorageGetData
  case class Conflicted(data: Data) extends StorageGetData
  case object None                  extends StorageGetData
}

trait PluggableStorage {
  def get(id: UUID): Future[StorageGetData]
  def put(data: Data): Future[Unit]
}
