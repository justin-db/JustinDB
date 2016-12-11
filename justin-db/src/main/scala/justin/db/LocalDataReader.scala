package justin.db

import java.util.UUID

import justin.db.StorageNodeActorProtocol._
import justin.db.storage.PluggableStorageProtocol.StorageGetData
import justin.db.storage.PluggableStorageProtocol

import scala.concurrent.{ExecutionContext, Future}

class LocalDataReader(storage: PluggableStorageProtocol)(implicit ec: ExecutionContext) {
  def apply(id: UUID): Future[StorageNodeReadingResult] = {
    storage.get(id).map {
      case StorageGetData.Single(data)             => StorageNodeReadingResult.Found(data)
      case StorageGetData.None                     => StorageNodeReadingResult.NotFound
      case StorageGetData.Conflicted(data1, data2) => StorageNodeReadingResult.Conflicted(data1, data2)
    } recover {
      case _                                       => StorageNodeReadingResult.FailedRead
    }
  }
}
