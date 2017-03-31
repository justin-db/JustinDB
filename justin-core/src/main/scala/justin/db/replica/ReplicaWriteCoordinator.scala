package justin.db.replica

import java.util.UUID

import justin.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db._
import justin.db.actors.protocol._
import justin.db.replica.ReplicaWriteAgreement.WriteAgreement

import scala.concurrent.{ExecutionContext, Future}

class ReplicaWriteCoordinator(
  nodeId: NodeId, ring: Ring, n: N,
  localDataWriter: ReplicaLocalWriter,
  remoteDataWriter: ReplicaRemoteWriter
)(implicit ec: ExecutionContext) extends ((StorageNodeWriteRequest, ClusterMembers) => Future[StorageNodeWriteResponse]) {

  override def apply(cmd: StorageNodeWriteRequest, clusterMembers: ClusterMembers): Future[StorageNodeWriteResponse] = cmd match {
    case StorageNodeWriteDataLocal(data)         => writeLocal(data)
    case StorageNodeWriteRequest.Replicate(w, data) => coordinateReplicated(w, data, clusterMembers)
  }

  private def writeLocal(data: Data) = localDataWriter.apply(data, new IsPrimaryOrReplica(nodeId, ring))

  private def coordinateReplicated(w: W, data: Data, clusterMembers: ClusterMembers) = {
    val ringPartitionId = UUID2RingPartitionId.apply(data.id, ring)
    PreferenceList(ringPartitionId, n, ring).fold(onLeft(data.id), onRight(w, data, clusterMembers))
  }

  private def onLeft(id: UUID)(err: PreferenceList.Error) = Future.successful(StorageNodeFailedWrite(id))

  private def onRight(w: W, data: Data, clusterMembers: ClusterMembers)(preferenceList: PreferenceList) = {
    val updatedData = Data.updateVclock(data, preferenceList)
    makeWrites(w, updatedData, clusterMembers, preferenceList)
      .map(new ReplicaWriteAgreement().reach(w))
      .map(consensus2WritingResult(updatedData.id))
  }

  private def makeWrites(w: W, updatedData: Data, clusterMembers: ClusterMembers, preferenceList: PreferenceList) = {
    ResolveNodeAddresses(nodeId, preferenceList, clusterMembers) match {
      case ResolvedNodeAddresses(true, remotes)  if remotes.size + 1 >= w.w => (writeLocal(updatedData) zip remoteDataWriter(remotes, updatedData)).map(converge)
      case ResolvedNodeAddresses(false, remotes) if remotes.size     >= w.w => remoteDataWriter(remotes, updatedData)
      case _                                                                => Future.successful(List(StorageNodeFailedWrite(updatedData.id)))
    }
  }

  private def consensus2WritingResult(id: UUID): WriteAgreement => StorageNodeWriteResponse = {
    case WriteAgreement.NotEnoughWrites => StorageNodeFailedWrite(id)
    case WriteAgreement.Ok              => StorageNodeSuccessfulWrite(id)
  }
}
