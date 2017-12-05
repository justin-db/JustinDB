package justin.db.storage.provider

import justin.db.storage.PluggableStorageProtocol

trait StorageProvider {
  def name: String
  def init: PluggableStorageProtocol
}

object StorageProvider {
  def apply(clazz: String): StorageProvider = Class.forName(clazz).newInstance().asInstanceOf[StorageProvider]
}
