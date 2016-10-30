package justin.db

import scala.collection.mutable

trait PluggableStorage {
  def get(key: String): Option[String]
  def put(key: String, value: String): Unit
}

class InMemStorage extends PluggableStorage {
  private var values = mutable.Map.empty[String, String]

  override def get(key: String): Option[String] = values.get(key)
  override def put(key: String, value: String): Unit = { values = values + ((key,value)) }
}
