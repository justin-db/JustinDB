package justin.db.replica

import java.util.UUID

import justin.db.actors.protocol.{StorageNodeFailedRead, StorageNodeFoundRead, StorageNodeNotFoundRead, StorageNodeReadResponse}
import justin.db.storage.GetStorageProtocol
import justin.db.storage.PluggableStorageProtocol.StorageGetData

import scala.concurrent.{ExecutionContext, Future}

class ReplicaLocalReader(storage: GetStorageProtocol)(implicit ec: ExecutionContext) {

  def apply(id: UUID, isPrimaryOrReplica: IsPrimaryOrReplica): Future[StorageNodeReadResponse] = {
    storage.get(id)(isPrimaryOrReplica).map {
      case StorageGetData.Single(justinData) => StorageNodeFoundRead(justinData)
      case StorageGetData.None               => StorageNodeNotFoundRead(id)
    } recover { case _                       => StorageNodeFailedRead(id) }
  }
}
