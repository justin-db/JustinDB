package justin.db

import justin.consistent_hashing.NodeId
import justin.db.StorageNodeActorProtocol._
import justin.db.storage.PluggableStorageProtocol.{StorageGetData, StoragePutData}
import justin.db.storage.PluggableStorageProtocol
import justin.db.versioning.VectorClockComparator.VectorClockRelation
import justin.db.versioning.{VCs2Compare, VectorClockComparator}

import scala.concurrent.{ExecutionContext, Future}

class LocalDataWriter(storage: PluggableStorageProtocol)(implicit ec: ExecutionContext) {

  private val vectorClockComparator = new VectorClockComparator[NodeId]

  def apply(data: Data): Future[StorageNodeWritingResult] = {
    storage.get(data.id).flatMap {
      case StorageGetData.None                           =>
        storage.put(StoragePutData.Single(data)).map(_ => StorageNodeWritingResult.SuccessfulWrite)
      case StorageGetData.Conflicted(existed1, existed2) =>
        ??? // TODO: finish
      case StorageGetData.Single(existed)                =>
        vectorClockComparator.apply(VCs2Compare(existed.vclock, data.vclock)) match {
          case VectorClockRelation.Predecessor           =>
            Future.successful(StorageNodeWritingResult.FailedWrite)
          case VectorClockRelation.Conflict              =>
            storage.put(StoragePutData.Conflict(existed.id, existed, data)).map(_ => StorageNodeWritingResult.ConflictedWrite)
          case VectorClockRelation.Consequent            =>
            storage.put(StoragePutData.Single(data)).map(_ => StorageNodeWritingResult.SuccessfulWrite)
        }
    } recover { case _ => StorageNodeWritingResult.FailedWrite }
  }
}
