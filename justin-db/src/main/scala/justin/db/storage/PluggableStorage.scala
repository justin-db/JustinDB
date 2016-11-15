package justin.db.storage

import scala.concurrent.Future

trait PluggableStorage {
  def get(key: String): Future[Option[String]]
  def put(key: String, value: String): Future[Unit]
}
