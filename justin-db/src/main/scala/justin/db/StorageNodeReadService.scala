package justin.db

import java.util.UUID

import justin.db.StorageNodeActor.{StorageNodeReadData, StorageNodeReadingResult}
import justin.db.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db.replication.{N, PreferenceList}
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
      for {
        preferenceList <- Future.successful(buildPreferenceList(id))
        localTarget     = buildLocalTargetOpt(preferenceList)
        remoteTargets   = buildRemoteTargets(preferenceList)
        allReads       <- readFromTargets(id, localTarget, remoteTargets)
      } yield {
        val onlyFoundReads  = allReads.collect { case r: StorageNodeReadingResult.Found => r }
        val onlyFailedReads = allReads.forall(_ == StorageNodeReadingResult.FailedRead)

        (onlyFoundReads.size >= r.r, onlyFoundReads.headOption, onlyFailedReads) match {
          case (true, Some(exemplary), _) => exemplary
          case (_, _, true)               => StorageNodeReadingResult.FailedRead
          case _                          => StorageNodeReadingResult.NotFound
        }
      }
  }

  private def buildPreferenceList(id: UUID) = {
    val basePartitionId = new UUID2RingPartitionId(ring).apply(id)
    PreferenceList(basePartitionId, n, ring)
  }

  private def buildLocalTargetOpt(preferenceList: List[NodeId]) = {
    preferenceList.find(_ == nodeId)
  }

  private def buildRemoteTargets(preferenceList: List[NodeId]) = {
    preferenceList.filterNot(_ == nodeId).distinct.flatMap(clusterMembers.get)
  }

  private def readFromTargets(id: UUID, localTargetOpt: Option[NodeId], remoteTargets: List[StorageNodeActorRef]) = {
    lazy val remoteReads = remoteReading.apply(remoteTargets, id)
    lazy val localRead   = localReading.apply(id)

    localTargetOpt.fold(remoteReads) { _ =>
      localRead.zip(remoteReads).map { case (lReadResult, rReadResults) => lReadResult :: rReadResults }
    }
  }
}
