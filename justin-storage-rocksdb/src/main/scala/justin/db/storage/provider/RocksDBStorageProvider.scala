package justin.db.storage.provider

import java.io.File

import justin.db.storage.{PluggableStorageProtocol, RocksDBStorage}
import justin.db.storage.config.StorageConfig

class RocksDBStorageProvider extends StorageProvider {
  override def init: PluggableStorageProtocol = {
    val dir = new File(StorageConfig.storage.rocksdb.`journal-path`)
    new RocksDBStorage(dir)
  }

  override def name: String = StorageConfig.storage.rocksdb.name
}
