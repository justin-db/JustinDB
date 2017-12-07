package justin.db.storage.provider

import justin.db.storage.config.StorageConfig
import justin.db.storage.{InMemStorage, PluggableStorageProtocol}

class InMemStorageProvider extends StorageProvider {

  override def init: PluggableStorageProtocol = new InMemStorage

  override def name: String = StorageConfig.storage.inmemory.name
}
