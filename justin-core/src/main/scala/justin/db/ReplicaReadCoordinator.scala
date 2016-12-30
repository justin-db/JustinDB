package justin.db

import justin.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db.StorageNodeActorProtocol._
import justin.db.replication.{N, PreferenceList, R}

import scala.concurrent.{ExecutionContext, Future}

class ReplicaReadCoordinator(
  nodeId: NodeId, ring: Ring, n: N,
  localDataReader: ReplicaLocalReader,
  remoteDataReader: ReplicaRemoteReader
)(implicit ec: ExecutionContext) extends ((StorageNodeReadData, ClusterMembers) => Future[StorageNodeReadingResult]) {
  import ReplicaReadCoordinator._

  override def apply(cmd: StorageNodeReadData, clusterMembers: ClusterMembers): Future[StorageNodeReadingResult] = cmd match {
    case StorageNodeReadData.Local(id)         => localDataReader.apply(id)
    case StorageNodeReadData.Replicated(r, id) =>
      val ringPartitionId = UUID2RingPartitionId.apply(id, ring)
      val preferenceList  = PreferenceList(ringPartitionId, n, ring)

      ResolveNodeTargets(nodeId, preferenceList, clusterMembers) match {
        case ResolvedTargets(true, remotes)  if remotes.size + 1 >= r.r =>
          (localDataReader.apply(id) zip remoteDataReader.apply(remotes, id)).map(converge).map(reachConsensus(r))
        case ResolvedTargets(false, remotes) if remotes.size     >= r.r =>
          remoteDataReader.apply(remotes, id).map(reachConsensus(r))
        case _ => Future.successful(StorageNodeReadingResult.FailedRead)
      }
  }
}

object ReplicaReadCoordinator {

  def reachConsensus(r: R): List[StorageNodeReadingResult] => StorageNodeReadingResult = { reads =>
    val onlyFoundReads = reads.collect { case r: StorageNodeReadingResult.Found => r }
    val onlyFailedReads = reads.forall(_ == StorageNodeReadingResult.FailedRead)

    (onlyFoundReads.size >= r.r, onlyFoundReads.headOption, onlyFailedReads) match {
      case (true, Some(exemplary), _) => exemplary
      case (_, _, true)               => StorageNodeReadingResult.FailedRead
      case _                          => StorageNodeReadingResult.NotFound
    }
  }
}
