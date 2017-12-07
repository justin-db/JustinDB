package justin.db.storage.provider

import justin.db.storage.RocksDBStorage
import org.scalatest.{FlatSpec, Matchers}

class RocksDBStorageProviderTest  extends FlatSpec with Matchers {

  it should "init RocksDB storage" in {
    val provider = StorageProvider.apply("justin.db.storage.provider.RocksDBStorageProvider").asInstanceOf[RocksDBStorageProvider]

    provider.name shouldBe "RocksDB storage"
    provider.init shouldBe a[RocksDBStorage]
  }
}
