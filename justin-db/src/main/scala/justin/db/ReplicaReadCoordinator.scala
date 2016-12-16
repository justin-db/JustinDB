package justin.db

import java.util.UUID

import justin.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db.StorageNodeActorProtocol._
import justin.db.replication.{N, PreferenceList, R}

import scala.concurrent.{ExecutionContext, Future}

class ReplicaReadCoordinator(nodeId: NodeId, clusterMembers: ClusterMembers, ring: Ring, n: N,
                             localDataReader: LocalDataReader,
                             remoteDataReader: RemoteDataReader)(implicit ec: ExecutionContext)
  extends (StorageNodeReadData => Future[StorageNodeReadingResult]) {

  override def apply(cmd: StorageNodeReadData): Future[StorageNodeReadingResult] = cmd match {
    case StorageNodeReadData.Local(id)         => localDataReader.apply(id)
    case StorageNodeReadData.Replicated(r, id) =>
      val ringPartitionId = UUID2RingPartitionId.apply(id, ring)
      val preferenceList  = PreferenceList(ringPartitionId, n, ring)

      readFromTargets(id, preferenceList).map(sumUpReads(r))
  }

  // TODO: what if one of the replica is conflicted?
  private def sumUpReads(r: R)(reads: List[StorageNodeReadingResult]) = {
    val onlyFoundReads  = reads.collect { case r: StorageNodeReadingResult.Found => r }
    val onlyFailedReads = reads.forall(_ == StorageNodeReadingResult.FailedRead)

    (onlyFoundReads.size >= r.r, onlyFoundReads.headOption, onlyFailedReads) match {
      case (true, Some(exemplary), _) => exemplary
      case (_, _, true)               => StorageNodeReadingResult.FailedRead
      case _                          => StorageNodeReadingResult.NotFound
    }
  }

  private def readFromTargets(id: UUID, preferenceList: List[NodeId]) = {
    val localTargetOpt = preferenceList.find(_ == nodeId)
    val remoteTargets  = preferenceList.filterNot(_ == nodeId).distinct.flatMap(clusterMembers.get)

    lazy val getRemoteReads = remoteDataReader.apply(remoteTargets, id)
    lazy val getLocalRead   = localDataReader.apply(id)

    localTargetOpt.fold(getRemoteReads)(_ => getLocalRead zip getRemoteReads map converge)
  }
}
