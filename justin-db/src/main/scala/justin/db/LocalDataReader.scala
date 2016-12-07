package justin.db

import java.util.UUID

import justin.db.StorageNodeActorProtocol._
import justin.db.storage.{PluggableStorage, StorageGetData}

import scala.concurrent.{ExecutionContext, Future}

class LocalDataReader(storage: PluggableStorage)(implicit ec: ExecutionContext) {
  def apply(id: UUID): Future[StorageNodeReadingResult] = {
    storage.get(id).map {
      case StorageGetData.Single(data)             => StorageNodeReadingResult.Found(data)
      case StorageGetData.None                     => StorageNodeReadingResult.NotFound
      case StorageGetData.Conflicted(data1, data2) => ??? // TODO
    }.recover {
      case _          => StorageNodeReadingResult.FailedRead
    }
  }
}
