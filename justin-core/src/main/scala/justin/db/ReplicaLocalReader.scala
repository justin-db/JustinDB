package justin.db

import java.util.UUID

import justin.db.StorageNodeActorProtocol._
import justin.db.storage.GetStorageProtocol
import justin.db.storage.PluggableStorageProtocol.StorageGetData

import scala.concurrent.{ExecutionContext, Future}

class ReplicaLocalReader(storage: GetStorageProtocol)(implicit ec: ExecutionContext) {

  def apply(id: UUID, resolveDataOriginality: ResolveDataOriginality): Future[StorageNodeReadingResult] = {
    storage.get(id)(resolveDataOriginality).map {
      case StorageGetData.Single(data)             => StorageNodeReadingResult.Found(data)
      case StorageGetData.None                     => StorageNodeReadingResult.NotFound
      case StorageGetData.Conflicted(data1, data2) => StorageNodeReadingResult.Conflicted(data1, data2)
    } recover { case _                             => StorageNodeReadingResult.FailedRead }
  }
}
