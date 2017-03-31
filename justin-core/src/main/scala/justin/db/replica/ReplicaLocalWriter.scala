package justin.db.replica

import justin.db.actors.protocol.{StorageNodeFailedWrite, StorageNodeSuccessfulWrite, StorageNodeWriteResponse}
import justin.db.Data
import justin.db.storage.PluggableStorageProtocol.{StorageGetData, StoragePutData}
import justin.db.storage.{GetStorageProtocol, PutStorageProtocol}
import justin.db.versioning.VectorClockComparator
import justin.db.versioning.VectorClockComparator.VectorClockRelation

import scala.concurrent.{ExecutionContext, Future}

class ReplicaLocalWriter(storage: GetStorageProtocol with PutStorageProtocol)(implicit ec: ExecutionContext) {

  def apply(newData: Data, isPrimaryOrReplica: IsPrimaryOrReplica): Future[StorageNodeWriteResponse] = {
    storage.get(newData.id)(isPrimaryOrReplica).flatMap {
      case StorageGetData.None            => putSingleSuccessfulWrite(newData, isPrimaryOrReplica)
      case StorageGetData.Single(oldData) => handleExistedSingleData(oldData, newData, isPrimaryOrReplica)
    } recover { case _                    => StorageNodeFailedWrite(newData.id) }
  }

  private def handleExistedSingleData(oldData: Data, newData: Data, isPrimaryOrReplica: IsPrimaryOrReplica) = {
    new VectorClockComparator().apply(oldData.vclock, newData.vclock) match {
      case VectorClockRelation.Predecessor => Future.successful(StorageNodeFailedWrite(newData.id))
      case VectorClockRelation.Conflict    => Future.successful(StorageNodeWriteResponse.ConflictedWrite(oldData, newData))
      case VectorClockRelation.Consequent  => putSingleSuccessfulWrite(newData, isPrimaryOrReplica)
    }
  }

  private def putSingleSuccessfulWrite(newData: Data, resolveDataOriginality: IsPrimaryOrReplica) = {
    storage.put(StoragePutData(newData))(resolveDataOriginality).map(_ => StorageNodeSuccessfulWrite(newData.id))
  }
}
