package justin.db.storage

import java.util.UUID
import justin.db.storage.PluggableStorageProtocol.{Ack, DataOriginality, StorageGetData, StoragePutData}

import scala.concurrent.{ExecutionContext, Future}

class PersistentStorage(implicit ec: ExecutionContext) extends PluggableStorageProtocol {
  println("PERSISTENT STROAGE LOADED")

  override def put(cmd: StoragePutData)(resolveOriginality: (UUID) => DataOriginality): Future[Ack] = ???

  override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = ???
}
