package justin.db.storage

import java.util.UUID

import justin.db.storage.PluggableStorageProtocol.{Ack, StorageGetData, StoragePutData}

import scala.concurrent.{ExecutionContext, Future}

class FilePerKeyStorage(implicit ec: ExecutionContext) extends PluggableStorageProtocol {
  override def get(id: UUID): Future[StorageGetData]  = ???
  override def put(cmd: StoragePutData): Future[Ack] = ???
}

object FilePerKeyStorage {
  def fileNameWithTxtExtension(fileName: String): String = s"$fileName.txt"
}
