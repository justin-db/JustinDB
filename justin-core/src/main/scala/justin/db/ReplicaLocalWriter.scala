package justin.db

import justin.consistent_hashing.NodeId
import justin.db.StorageNodeActorProtocol._
import justin.db.storage.PluggableStorageProtocol
import justin.db.storage.PluggableStorageProtocol.{StorageGetData, StoragePutData}
import justin.db.versioning.VectorClockComparator.VectorClockRelation
import justin.db.versioning.{NodeIdVectorClock, VCs2Compare, VectorClockComparator}

import scala.concurrent.{ExecutionContext, Future}

class ReplicaLocalWriter(storage: PluggableStorageProtocol)(implicit ec: ExecutionContext) {

  private val vectorClockComparator = new VectorClockComparator[NodeId]

  def apply(data: Data, resolveDataOriginality: ResolveDataOriginality): Future[StorageNodeWritingResult] = {
    storage.get(data.id)(resolveDataOriginality).flatMap {
      case StorageGetData.None                   => putSingleSuccessfulWrite(data)
      case conflicted: StorageGetData.Conflicted => handleExistedConflictData(data, conflicted)
      case single: StorageGetData.Single         => handleExistedSingleData(data, single)
    } recover {
      case _                                     => StorageNodeWritingResult.FailedWrite
    }
  }

  private def handleExistedConflictData(data: Data, conflicted: StorageGetData.Conflicted) = {
    import VectorClockRelation._

    val vcCmpRes  = compareVectorClocks(conflicted.data1.vclock, data.vclock)
    val vcCmpRes2 = compareVectorClocks(conflicted.data2.vclock, data.vclock)

    (vcCmpRes, vcCmpRes2) match {
      case (Consequent, Consequent) => putSingleSuccessfulWrite(data)
      case _                        => Future.successful(StorageNodeWritingResult.FailedWrite)
    }
  }

  private def handleExistedSingleData(data: Data, single: StorageGetData.Single) = {
    compareVectorClocks(single.data.vclock, data.vclock) match {
      case VectorClockRelation.Predecessor => Future.successful(StorageNodeWritingResult.FailedWrite)
      case VectorClockRelation.Conflict    => putConflictSuccessfulWrite(StoragePutData.Conflict(single.data.id, single.data, data))
      case VectorClockRelation.Consequent  => putSingleSuccessfulWrite(data)
    }
  }

  private def putSingleSuccessfulWrite(data: Data) = {
    storage.put(StoragePutData.Single(data)).map(_ => StorageNodeWritingResult.SuccessfulWrite)
  }

  private def putConflictSuccessfulWrite(conflict: StoragePutData.Conflict) = {
    storage.put(conflict).map(_ => StorageNodeWritingResult.ConflictedWrite)
  }

  private def compareVectorClocks(vclock: NodeIdVectorClock, vclock2: NodeIdVectorClock) = {
    vectorClockComparator.apply(VCs2Compare(vclock, vclock2))
  }
}
