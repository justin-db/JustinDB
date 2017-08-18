package justin.db.storage

import java.util.UUID
import justin.db.storage.PluggableStorageProtocol.{Ack, DataOriginality, StorageGetData, StoragePutData}

import scala.concurrent.{ExecutionContext, Future}

class PersistentStorage extends PluggableStorageProtocol {
  println("PERSISTENT STORAGE LOADED")

  override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = ???

  override def put(cmd: StoragePutData)(resolveOriginality: (UUID) => DataOriginality): Future[Ack] = ???
}
