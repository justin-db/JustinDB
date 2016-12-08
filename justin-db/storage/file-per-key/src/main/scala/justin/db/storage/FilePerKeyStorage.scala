package justin.db.storage

import java.util.UUID

import FilePerKeyStorage._
import justin.db.Data

import scala.concurrent.{ExecutionContext, Future}

class FilePerKeyStorage(implicit ec: ExecutionContext) extends PluggableStorageProtocol {
  override def get(id: UUID): Future[StorageGetData] = ???
  override def put(data: Data): Future[Unit] = ???
}

object FilePerKeyStorage {
  def fileNameWithTxtExtenstion(fileName: String) = s"$fileName.txt"
}
