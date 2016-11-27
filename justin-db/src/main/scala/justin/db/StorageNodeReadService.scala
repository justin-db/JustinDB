package justin.db

import java.util.UUID

import justin.db.StorageNodeActorProtocol._
import justin.db.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db.replication.{N, PreferenceList, R}
import justin.db.storage.PluggableStorage

import scala.concurrent.{ExecutionContext, Future}

class StorageNodeReadService(nodeId: NodeId, clusterMembers: ClusterMembers,
                             ring: Ring, n: N, storage: PluggableStorage)(implicit ec: ExecutionContext)
  extends (StorageNodeReadData => Future[StorageNodeReadingResult]) {

  private val localReading  = new LocalDataReadingService(storage)
  private val remoteReading = new RemoteDataReadingService

  override def apply(cmd: StorageNodeReadData): Future[StorageNodeReadingResult] = cmd match {
    case StorageNodeReadData.Local(id)         => localReading.apply(id)
    case StorageNodeReadData.Replicated(r, id) =>
      val ringPartitionId = UUID2RingPartitionId.apply(id, ring)
      val preferenceList  = PreferenceList(ringPartitionId, n, ring)
      readFromTargets(id, preferenceList).map(sumUpReads(r))
  }

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

    lazy val getRemoteReads = remoteReading.apply(remoteTargets, id)
    lazy val getLocalRead   = localReading.apply(id)

    localTargetOpt.fold(getRemoteReads)(_ => getLocalRead zip getRemoteReads map converge)
  }

  private def converge(result: (StorageNodeReadingResult, List[StorageNodeReadingResult])) = result._1 :: result._2
}
