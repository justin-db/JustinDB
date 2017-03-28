package justin.db.storage

import java.util.UUID

import justin.db.storage.PluggableStorageProtocol.{Ack, DataOriginality, StorageGetData, StoragePutData}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class JustinDriverTest extends FlatSpec with Matchers {

  behavior of "JustinDriver"

  it should "create instance of storage based on protocol interface" in {
    JustinDriver.load("justin.db.storage.PluggableStorageProtocolImpl") shouldBe a[PluggableStorageProtocol]
  }
}

private class PluggableStorageProtocolImpl extends PluggableStorageProtocol {
  override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality)(implicit ec: ExecutionContext): Future[StorageGetData] = ???
  override def put(cmd: StoragePutData)(resolveOriginality: (UUID) => DataOriginality)(implicit ec: ExecutionContext): Future[Ack] = ???
}
