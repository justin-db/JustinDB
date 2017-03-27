package justin.db

import justin.db.actors.StorageNodeActorProtocol.StorageNodeWritingResult
import justin.db.storage.PluggableStorageProtocol.{StorageGetData, StoragePutData}
import storage.{GetStorageProtocol, PutStorageProtocol}
import justin.db.versioning.VectorClockComparator
import justin.db.versioning.VectorClockComparator.VectorClockRelation

import scala.concurrent.{ExecutionContext, Future}

class ReplicaLocalWriter(storage: GetStorageProtocol with PutStorageProtocol)(implicit ec: ExecutionContext) {

  def apply(newData: Data, resolveDataOriginality: ResolveDataOriginality): Future[StorageNodeWritingResult] = {
    storage.get(newData.id)(resolveDataOriginality).flatMap {
      case StorageGetData.None            => putSingleSuccessfulWrite(newData, resolveDataOriginality)
      case StorageGetData.Single(oldData) => handleExistedSingleData(oldData, newData, resolveDataOriginality)
    } recover { case _                    => StorageNodeWritingResult.FailedWrite }
  }

  private def handleExistedSingleData(oldData: Data, newData: Data, resolveDataOriginality: ResolveDataOriginality) = {
    new VectorClockComparator().apply(oldData.vclock, newData.vclock) match {
      case VectorClockRelation.Predecessor => Future.successful(StorageNodeWritingResult.FailedWrite)
      case VectorClockRelation.Conflict    => Future.successful(StorageNodeWritingResult.ConflictedWrite(oldData, newData))
      case VectorClockRelation.Consequent  => putSingleSuccessfulWrite(newData, resolveDataOriginality)
    }
  }

  private def putSingleSuccessfulWrite(newData: Data, resolveDataOriginality: ResolveDataOriginality) = {
    storage.put(StoragePutData(newData))(resolveDataOriginality).map(_ => StorageNodeWritingResult.SuccessfulWrite)
  }
}
