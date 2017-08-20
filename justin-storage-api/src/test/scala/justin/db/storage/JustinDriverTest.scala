package justin.db.storage

import java.io.File
import java.util.UUID

import justin.db.storage.PluggableStorageProtocol.{Ack, DataOriginality, StorageGetData, StoragePutData}
import justin.db.storage.drivers.{PersistentStorage, PluggableStorageProtocolImpl}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class JustinDriverTest extends FlatSpec with Matchers {

  behavior of "JustinDriver"

  it should "create instance of storage based on protocol interface" in {
    JustinDriver.load("justin.db.storage.drivers.PluggableStorageProtocolImpl")(null) shouldBe a[PluggableStorageProtocolImpl]
  }

  it should "instantiate PersistentStorage with specified journal file" in {
    JustinDriver.load("justin.db.storage.drivers.PersistentStorage")(File.createTempFile("journal", ".tmp")) shouldBe a[PersistentStorage]
  }
}

package drivers {
  class PluggableStorageProtocolImpl extends PluggableStorageProtocol {
    override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = ???
    override def put(cmd: StoragePutData)(resolveOriginality: (UUID) => DataOriginality): Future[Ack] = ???
  }

  class PersistentStorage(journalFile: File) extends PluggableStorageProtocol {

    override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = ???
    override def put(cmd: StoragePutData)(resolveOriginality: (UUID) => DataOriginality): Future[Ack] = ???
  }
}
