package justin.db.storage

class InMemStorage extends PluggableStorage {
  import scala.collection.mutable

  private var values = mutable.Map.empty[String, String]

  override def get(key: String): Option[String] = values.get(key)
  override def put(key: String, value: String): Unit = { values = values + ((key,value)) }
}
