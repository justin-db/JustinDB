package justin.db

import justin.db.StorageNodeActorProtocol._
import justin.db.storage.PluggableStorageProtocol.{StorageGetData, StoragePutData}
import justin.db.storage.{GetStorageProtocol, PutStorageProtocol}
import justin.db.versioning.VectorClockComparator.VectorClockRelation
import justin.db.versioning.VectorClockComparator.VectorClockRelation._
import justin.db.versioning.{NodeIdVectorClock, VCs2Compare, VectorClockComparator}

import scala.concurrent.{ExecutionContext, Future}

class ReplicaLocalWriter(storage: GetStorageProtocol with PutStorageProtocol)(implicit ec: ExecutionContext) {

  def apply(data: Data, resolveDataOriginality: ResolveDataOriginality): Future[StorageNodeWritingResult] = {
    storage.get(data.id)(resolveDataOriginality).flatMap {
      case StorageGetData.None                   => putSingleSuccessfulWrite(data, resolveDataOriginality)
      case conflicted: StorageGetData.Conflicted => handleExistedConflictData(data, conflicted, resolveDataOriginality)
      case single: StorageGetData.Single         => handleExistedSingleData(data, single, resolveDataOriginality)
    } recover {
      case _                                     => StorageNodeWritingResult.FailedWrite
    }
  }

  private def handleExistedConflictData(data: Data, conflicted: StorageGetData.Conflicted, resolveDataOriginality: ResolveDataOriginality) = {
    val vcCmpRes  = compareVectorClocks(conflicted.data1.vclock, data.vclock)
    val vcCmpRes2 = compareVectorClocks(conflicted.data2.vclock, data.vclock)

    (vcCmpRes, vcCmpRes2) match {
      case (Consequent, Consequent) => putSingleSuccessfulWrite(data, resolveDataOriginality)
      case _                        => Future.successful(StorageNodeWritingResult.FailedWrite)
    }
  }

  private def handleExistedSingleData(data: Data, single: StorageGetData.Single, resolveDataOriginality: ResolveDataOriginality) = {
    compareVectorClocks(single.data.vclock, data.vclock) match {
      case VectorClockRelation.Predecessor => Future.successful(StorageNodeWritingResult.FailedWrite)
      case VectorClockRelation.Conflict    => putConflictSuccessfulWrite(StoragePutData.Conflict(single.data.id, single.data, data), resolveDataOriginality)
      case VectorClockRelation.Consequent  => putSingleSuccessfulWrite(data, resolveDataOriginality)
    }
  }

  private def putSingleSuccessfulWrite(data: Data, resolveDataOriginality: ResolveDataOriginality) = {
    storage.put(StoragePutData.Single(data))(resolveDataOriginality).map(_ => StorageNodeWritingResult.SuccessfulWrite)
  }

  private def putConflictSuccessfulWrite(conflict: StoragePutData.Conflict, resolveDataOriginality: ResolveDataOriginality) = {
    storage.put(conflict)(resolveDataOriginality).map(_ => StorageNodeWritingResult.ConflictedWrite)
  }

  private def compareVectorClocks(vclock: NodeIdVectorClock, vclock2: NodeIdVectorClock) = {
    new VectorClockComparator().apply(VCs2Compare(vclock, vclock2))
  }
}
