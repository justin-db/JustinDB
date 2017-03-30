package justin.db.replica

import justin.db.actors.protocol.{StorageNodeSuccessfulWrite, StorageNodeWritingResult}
import justin.db.Data
import justin.db.storage.PluggableStorageProtocol.{StorageGetData, StoragePutData}
import justin.db.storage.{GetStorageProtocol, PutStorageProtocol}
import justin.db.versioning.VectorClockComparator
import justin.db.versioning.VectorClockComparator.VectorClockRelation

import scala.concurrent.{ExecutionContext, Future}

class ReplicaLocalWriter(storage: GetStorageProtocol with PutStorageProtocol)(implicit ec: ExecutionContext) {

  def apply(newData: Data, isPrimaryOrReplica: IsPrimaryOrReplica): Future[StorageNodeWritingResult] = {
    storage.get(newData.id)(isPrimaryOrReplica).flatMap {
      case StorageGetData.None            => putSingleSuccessfulWrite(newData, isPrimaryOrReplica)
      case StorageGetData.Single(oldData) => handleExistedSingleData(oldData, newData, isPrimaryOrReplica)
    } recover { case _                    => StorageNodeWritingResult.FailedWrite }
  }

  private def handleExistedSingleData(oldData: Data, newData: Data, isPrimaryOrReplica: IsPrimaryOrReplica) = {
    new VectorClockComparator().apply(oldData.vclock, newData.vclock) match {
      case VectorClockRelation.Predecessor => Future.successful(StorageNodeWritingResult.FailedWrite)
      case VectorClockRelation.Conflict    => Future.successful(StorageNodeWritingResult.ConflictedWrite(oldData, newData))
      case VectorClockRelation.Consequent  => putSingleSuccessfulWrite(newData, isPrimaryOrReplica)
    }
  }

  private def putSingleSuccessfulWrite(newData: Data, resolveDataOriginality: IsPrimaryOrReplica) = {
    storage.put(StoragePutData(newData))(resolveDataOriginality).map(_ => StorageNodeSuccessfulWrite(newData.id))
  }
}
