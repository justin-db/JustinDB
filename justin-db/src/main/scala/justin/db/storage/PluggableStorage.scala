package justin.db.storage

// TODO: make it async
trait PluggableStorage {
  def get(key: String): Option[String]
  def put(key: String, value: String): Unit
}
