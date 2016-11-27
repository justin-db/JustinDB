package justin.db

import java.util.UUID

import justin.db.StorageNodeActorProtocol._
import justin.db.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db.replication.{N, PreferenceList}
import justin.db.storage.PluggableStorage

import scala.concurrent.{ExecutionContext, Future}

class StorageNodeWriteService(nodeId: NodeId, clusterMembers: ClusterMembers,
                              ring: Ring, n: N, storage: PluggableStorage)(implicit ec: ExecutionContext)
  extends (StorageNodeWriteData => Future[StorageNodeWritingResult]) {

  private val localSaving  = new LocalDataSavingService(storage)
  private val remoteSaving = new RemoteDataSavingService()

  override def apply(cmd: StorageNodeWriteData): Future[StorageNodeWritingResult] = cmd match {
    case StorageNodeWriteData.Local(data)        => localSaving.apply(data)
    case StorageNodeWriteData.Replicate(w, data) =>
      for {
        preferenceList <- Future.successful(buildPreferenceList(data.id))
        localTarget     = buildLocalTargetOpt(preferenceList)
        remoteTargets   = buildRemoteTargets(preferenceList)
        allWrites      <- writeToTargets(data, localTarget, remoteTargets)
        okWrites        = allWrites.count(_ == StorageNodeWritingResult.SuccessfulWrite)
      } yield {
        okWrites >= w.w match {
          case true  => StorageNodeWritingResult.SuccessfulWrite
          case false => StorageNodeWritingResult.FailedWrite
        }
      }
  }

  private def buildPreferenceList(id: UUID) = {
    val basePartitionId = UUID2RingPartitionId.apply(id, ring)
    PreferenceList(basePartitionId, n, ring)
  }

  private def buildLocalTargetOpt(preferenceList: List[NodeId]) = {
    preferenceList.find(_ == nodeId)
  }

  private def buildRemoteTargets(preferenceList: List[NodeId]) = {
    preferenceList.filterNot(_ == nodeId).distinct.flatMap(clusterMembers.get)
  }

  private def writeToTargets(data: Data, localTargetOpt: Option[NodeId], remoteTargets: List[StorageNodeActorRef]) = {
    lazy val remoteSaves = remoteSaving.apply(remoteTargets, data)
    lazy val localSave   = localSaving.apply(data)

    localTargetOpt.fold(remoteSaves) { _ =>
      localSave.zip(remoteSaves).map { case (lSaveResult, rSaveResults) => lSaveResult :: rSaveResults }
    }
  }
}
