package justin.db

import justin.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db.ConsensusReplicatedWrites.ConsensusSummary
import justin.db.StorageNodeActorProtocol._
import justin.db.replication.{N, PreferenceList, W}

import scala.concurrent.{ExecutionContext, Future}

class ReplicaWriteCoordinator(
  nodeId: NodeId, ring: Ring, n: N,
  localDataWriter: ReplicaLocalWriter,
  remoteDataWriter: ReplicaRemoteWriter
)(implicit ec: ExecutionContext) extends ((StorageNodeWriteData, ClusterMembers) => Future[StorageNodeWritingResult]) {

  override def apply(cmd: StorageNodeWriteData, clusterMembers: ClusterMembers): Future[StorageNodeWritingResult] = cmd match {
    case StorageNodeWriteData.Local(data)        => writeLocal(data)
    case StorageNodeWriteData.Replicate(w, data) => coordinateReplicated(w, data, clusterMembers)
  }

  private def writeLocal(data: Data) = localDataWriter.apply(data, new ResolveDataOriginality(nodeId, ring))

  private def coordinateReplicated(w: W, data: Data, clusterMembers: ClusterMembers) = {
    val ringPartitionId = UUID2RingPartitionId.apply(data.id, ring)
    PreferenceList(ringPartitionId, n, ring).fold(onLeft, onRight(w, data, clusterMembers))
  }

  private def onLeft(err: PreferenceList.Error) = Future.successful(StorageNodeWritingResult.FailedWrite)

  private def onRight(w: W, data: Data, clusterMembers: ClusterMembers)(preferenceList: PreferenceList) = {
    val updatedData = Data.updateVclock(data, preferenceList)
    makeWrites(w, updatedData, clusterMembers, preferenceList)
      .map(new ConsensusReplicatedWrites().reach(w))
      .map(consensus2WritingResult)
  }

  private def makeWrites(w: W, updatedData: Data, clusterMembers: ClusterMembers, preferenceList: PreferenceList) = {
    ResolveNodeAddresses(nodeId, preferenceList, clusterMembers) match {
      case ResolvedNodeAddresses(true, remotes)  if remotes.size + 1 >= w.w => (writeLocal(updatedData) zip remoteDataWriter.apply(remotes, updatedData)).map(converge)
      case ResolvedNodeAddresses(false, remotes) if remotes.size     >= w.w => remoteDataWriter.apply(remotes, updatedData)
      case _                                                                => Future.successful(List(StorageNodeWritingResult.FailedWrite))
    }
  }

  private def consensus2WritingResult: ConsensusSummary => StorageNodeWritingResult = {
    case ConsensusSummary.NotEnoughWrites => StorageNodeWritingResult.FailedWrite
    case ConsensusSummary.Ok              => StorageNodeWritingResult.SuccessfulWrite
  }
}
