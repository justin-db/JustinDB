package justin.db

import java.util.UUID

import justin.db.StorageNodeActorProtocol._
import justin.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db.replication.{N, PreferenceList}

import scala.concurrent.{ExecutionContext, Future}

class StorageNodeWriteService(nodeId: NodeId, clusterMembers: ClusterMembers, ring: Ring, n: N,
                              localDataSaver: LocalDataSavingService,
                              remoteDataSaver: RemoteDataSavingService)(implicit ec: ExecutionContext)
  extends (StorageNodeWriteData => Future[StorageNodeWritingResult]) {

  override def apply(cmd: StorageNodeWriteData): Future[StorageNodeWritingResult] = cmd match {
    case StorageNodeWriteData.Local(data)        => localDataSaver.apply(data)
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
    lazy val remoteSaves = remoteDataSaver.apply(remoteTargets, data)
    lazy val localSave   = localDataSaver.apply(data)

    localTargetOpt.fold(remoteSaves) { _ =>
      localSave.zip(remoteSaves).map { case (lSaveResult, rSaveResults) => lSaveResult :: rSaveResults }
    }
  }
}
