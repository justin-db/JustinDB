package justin.db.storage

import java.io.File
import java.util.UUID

import justin.db.storage.PluggableStorageProtocol.{Ack, DataOriginality, StorageGetData}
import logdb.LogDB

import scala.concurrent.Future

class PersistentStorage(journalFile: File) extends PluggableStorageProtocol {

  private[this] val logDB = {
    journalFile.createNewFile()
    new LogDB(journalFile)(new JustinLogDBSerializer)
  }

  override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = Future.successful {
    logDB.get(id)
      .map(StorageGetData.Single)
      .getOrElse(StorageGetData.None)
  }

  override def put(data: JustinData)(resolveOriginality: (UUID) => DataOriginality): Future[Ack] = {
    logDB.save(data.id, data)
    Ack.future
  }
}
