package justin.db

import justin.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db.StorageNodeActorProtocol._
import justin.db.replication.{N, PreferenceList, W}

import scala.concurrent.{ExecutionContext, Future}

class ReplicaWriteCoordinator(nodeId: NodeId, clusterMembers: ClusterMembers, ring: Ring, n: N,
                              localDataWriter: LocalDataWriter,
                              remoteDataWriter: RemoteDataWriter)(implicit ec: ExecutionContext)
  extends (StorageNodeWriteData => Future[StorageNodeWritingResult]) {

  override def apply(cmd: StorageNodeWriteData): Future[StorageNodeWritingResult] = cmd match {
    case StorageNodeWriteData.Local(data)        => localDataWriter.apply(data)
    case StorageNodeWriteData.Replicate(w, data) =>
      val ringPartitionId = UUID2RingPartitionId.apply(data.id, ring)
      val preferenceList  = PreferenceList(ringPartitionId, n, ring)
      val updatedData     = Data.updateVclock(data, preferenceList)

      writeToTargets(updatedData, preferenceList).map(sumUpWrites(w))
  }

  private def sumUpWrites(w: W)(writes: List[StorageNodeWritingResult]) = {
    val okWrites = writes.count(_ == StorageNodeWritingResult.SuccessfulWrite)

    okWrites >= w.w match {
      case true  => StorageNodeWritingResult.SuccessfulWrite
      case false => StorageNodeWritingResult.FailedWrite
    }
  }

  private def writeToTargets(data: Data, preferenceList: List[NodeId]) = {
    val localTargetOpt = preferenceList.find(_ == nodeId)
    val remoteTargets  = preferenceList.filterNot(_ == nodeId).distinct.flatMap(clusterMembers.get)

    lazy val getRemoteWrites = remoteDataWriter.apply(remoteTargets, data)
    lazy val getLocalWrite   = localDataWriter.apply(data)

    localTargetOpt.fold(getRemoteWrites)(_ => getLocalWrite zip getRemoteWrites map converge )
  }
}
