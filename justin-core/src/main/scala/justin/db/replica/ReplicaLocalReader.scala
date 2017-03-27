package justin.db.replica

import java.util.UUID

import justin.db.actors.protocol.StorageNodeReadingResult
import justin.db.storage.GetStorageProtocol
import justin.db.storage.PluggableStorageProtocol.StorageGetData

import scala.concurrent.{ExecutionContext, Future}

class ReplicaLocalReader(storage: GetStorageProtocol)(implicit ec: ExecutionContext) {

  def apply(id: UUID, isPrimaryOrReplica: IsPrimaryOrReplica): Future[StorageNodeReadingResult] = {
    storage.get(id)(isPrimaryOrReplica).map {
      case StorageGetData.Single(justinData) => StorageNodeReadingResult.Found(justinData)
      case StorageGetData.None               => StorageNodeReadingResult.NotFound
    } recover { case _                       => StorageNodeReadingResult.FailedRead }
  }
}
