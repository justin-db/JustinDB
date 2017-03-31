package justin.db.replica

import java.util.UUID

import justin.db.actors.protocol.{StorageNodeFoundRead, StorageNodeReadResponse}
import justin.db.storage.GetStorageProtocol
import justin.db.storage.PluggableStorageProtocol.StorageGetData

import scala.concurrent.{ExecutionContext, Future}

class ReplicaLocalReader(storage: GetStorageProtocol)(implicit ec: ExecutionContext) {

  def apply(id: UUID, isPrimaryOrReplica: IsPrimaryOrReplica): Future[StorageNodeReadResponse] = {
    storage.get(id)(isPrimaryOrReplica).map {
      case StorageGetData.Single(justinData) => StorageNodeFoundRead(justinData)
      case StorageGetData.None               => StorageNodeReadResponse.StorageNodeNotFoundRead(id)
    } recover { case _                       => StorageNodeReadResponse.FailedRead }
  }
}
