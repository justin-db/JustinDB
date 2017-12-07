package justin.db.storage.provider

import justin.db.storage.InMemStorage
import org.scalatest.{FlatSpec, Matchers}

class InMemStorageProviderTest extends FlatSpec with Matchers {

  it should "init In-Memory storage" in {
    val provider = StorageProvider.apply("justin.db.storage.provider.InMemStorageProvider").asInstanceOf[InMemStorageProvider]

    provider.name shouldBe "In-Mem Storage"
    provider.init shouldBe a[InMemStorage]
  }
}
