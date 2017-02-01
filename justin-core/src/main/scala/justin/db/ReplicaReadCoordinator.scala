package justin.db

import java.util.UUID

import justin.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db.StorageNodeActorProtocol._
import justin.db.replication.{N, PreferenceList, R}

import scala.concurrent.{ExecutionContext, Future}

class ReplicaReadCoordinator(
  nodeId: NodeId, ring: Ring, n: N,
  localDataReader: ReplicaLocalReader,
  remoteDataReader: ReplicaRemoteReader
)(implicit ec: ExecutionContext) extends ((StorageNodeReadData, ClusterMembers) => Future[StorageNodeReadingResult]) {

  override def apply(cmd: StorageNodeReadData, clusterMembers: ClusterMembers): Future[StorageNodeReadingResult] = cmd match {
    case StorageNodeReadData.Local(id)         => coordinateLocal(id)
    case StorageNodeReadData.Replicated(r, id) => coordinateReplicated(r, id, clusterMembers)
  }

  private def coordinateLocal(id: UUID) = localDataReader.apply(id, new ResolveDataOriginality(nodeId, ring))

  private def coordinateReplicated(r: R, id: UUID, clusterMembers: ClusterMembers) = {
    val ringPartitionId = UUID2RingPartitionId.apply(id, ring)

    PreferenceList(ringPartitionId, n, ring) match {
      case Left(PreferenceList.LackOfCoordinator)    => Future.successful(StorageNodeReadingResult.FailedRead)
      case Left(PreferenceList.NotSufficientSize(_)) => Future.successful(StorageNodeReadingResult.FailedRead)
      case Right(preferenceList)                     =>
        ResolveNodeTargets(nodeId, preferenceList, clusterMembers) match {
          case ResolvedTargets(true, remotes)  if remotes.size + 1 >= r.r =>
//            (coordinateLocal(id) zip remoteDataReader.apply(remotes, id)).map(converge).map(ReachConsensusReplicatedReads(r))
            ???
          case ResolvedTargets(false, remotes) if remotes.size     >= r.r =>
//            remoteDataReader.apply(remotes, id).map(ReachConsensusReplicatedReads(r))
            ???
          case _ => Future.successful(StorageNodeReadingResult.FailedRead)
        }
    }
  }
}
