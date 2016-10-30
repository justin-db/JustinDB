package justin.db

trait PluggableStorage {
  def get(key: String): Option[String]
  def put(key: String, value: String): Unit
}
