package justin.db.storage

import java.util.UUID
import justin.db.storage.PluggableStorageProtocol.{StorageGetData, StoragePutData}
import scala.concurrent.{ExecutionContext, Future}

class FilePerKeyStorage(implicit ec: ExecutionContext) extends PluggableStorageProtocol {
  override def get(id: UUID): Future[StorageGetData]  = ???
  override def put(cmd: StoragePutData): Future[Unit] = ???
}

object FilePerKeyStorage {
  def fileNameWithTxtExtension(fileName: String): String = s"$fileName.txt"
}
