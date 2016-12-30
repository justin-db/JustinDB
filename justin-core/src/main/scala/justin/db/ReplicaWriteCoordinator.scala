package justin.db

import justin.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db.StorageNodeActorProtocol._
import justin.db.replication.{N, PreferenceList, W}

import scala.concurrent.{ExecutionContext, Future}

class ReplicaWriteCoordinator(
  nodeId: NodeId, ring: Ring, n: N,
  localDataWriter: ReplicaLocalWriter,
  remoteDataWriter: ReplicaRemoteWriter
)(implicit ec: ExecutionContext) extends ((StorageNodeWriteData, ClusterMembers) => Future[StorageNodeWritingResult]) {
  import ReplicaWriteCoordinator._

  override def apply(cmd: StorageNodeWriteData, clusterMembers: ClusterMembers): Future[StorageNodeWritingResult] = cmd match {
    case StorageNodeWriteData.Local(data)        => localDataWriter.apply(data)
    case StorageNodeWriteData.Replicate(w, data) =>
      val ringPartitionId = UUID2RingPartitionId.apply(data.id, ring)
      val preferenceList  = PreferenceList(ringPartitionId, n, ring)
      val updatedData     = Data.updateVclock(data, preferenceList)

      ResolveNodeTargets(nodeId, preferenceList, clusterMembers) match {
        case ResolvedTargets(true, remotes)  if remotes.size + 1 >= w.w =>
          (localDataWriter.apply(updatedData) zip remoteDataWriter.apply(remotes, updatedData)).map(converge).map(reachConsensus(w))
        case ResolvedTargets(false, remotes) if remotes.size     >= w.w =>
          remoteDataWriter.apply(remotes, updatedData).map(reachConsensus(w))
        case _ => Future.successful(StorageNodeWritingResult.FailedWrite)
      }
  }
}

object ReplicaWriteCoordinator {

  def reachConsensus(w: W): List[StorageNodeWritingResult] => StorageNodeWritingResult = { writes =>
    val okWrites = writes.count(_ == StorageNodeWritingResult.SuccessfulWrite)

    okWrites >= w.w match {
      case true  => StorageNodeWritingResult.SuccessfulWrite
      case false => StorageNodeWritingResult.FailedWrite
    }
  }
}
