package justin.db.storage

import java.util.UUID

import justin.db.Data

import scala.concurrent.Future

trait PluggableStorage {
  def get(id: UUID): Future[Option[Data]]
  def put(data: Data): Future[Unit]
}
