package justin.db

import justin.db.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db.replication.{N, PreferenceList, W}
import justin.db.storage.PluggableStorage

import scala.concurrent.{ExecutionContext, Future}

class StorageNodeWriteService(nodeId: NodeId, clusterMembers: ClusterMembers,
                              ring: Ring, n: N, storage: PluggableStorage)(implicit ec: ExecutionContext)
  extends (StorageNodeWriteData => Future[StorageNodeWritingResult]) {

  private val localSaving  = new LocalDataSavingService(storage)
  private val remoteSaving = new RemoteDataSavingService()

  override def apply(cmd: StorageNodeWriteData): Future[StorageNodeWritingResult] = cmd match {
    case StorageNodeWriteData.Local(data)        =>
      localSaving.apply(data)
    case StorageNodeWriteData.Replicate(w, data) =>
      for {
        preferenceList <- Future.successful(buildPreferenceList(data))
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

  private def buildPreferenceList(data: Data) = {
    val basePartitionId = new UUID2RingPartitionId(ring).apply(data.id)
    PreferenceList(basePartitionId, n, ring)
  }

  private def buildLocalTargetOpt(preferenceList: List[NodeId]): Option[NodeId] = {
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

sealed trait StorageNodeWriteData
object StorageNodeWriteData {
  case class Local(data: Data)           extends StorageNodeWriteData
  case class Replicate(w: W, data: Data) extends StorageNodeWriteData
}

sealed trait StorageNodeWritingResult
object StorageNodeWritingResult {
  case object SuccessfulWrite extends StorageNodeWritingResult
  case object FailedWrite     extends StorageNodeWritingResult
}

class LocalDataSavingService(storage: PluggableStorage)(implicit ec: ExecutionContext) {
  def apply(data: Data): Future[StorageNodeWritingResult] = {
    storage.put(data.id.toString, data.value)
      .map(_ => StorageNodeWritingResult.SuccessfulWrite)
      .recover { case _ => StorageNodeWritingResult.FailedWrite }
  }
}

class RemoteDataSavingService(implicit ec: ExecutionContext) {
  import akka.pattern.ask
  import akka.util.Timeout
  import scala.concurrent.duration._

  private implicit val timeout = Timeout(3.seconds) // TODO: tune this value

  def apply(storageNodeRefs: List[StorageNodeActorRef], data: Data): Future[List[StorageNodeWritingResult]] = {
    val msg = StorageNodeActor.PutLocalValue(data.id, data.value)
    Future.sequence(storageNodeRefs.map(putLocalValue(_, msg)))
  }

  private def putLocalValue(node: StorageNodeActorRef, msg: StorageNodeActor.PutLocalValue): Future[StorageNodeWritingResult] = {
    (node.storageNodeActor ? msg)
      .map(_ => StorageNodeWritingResult.SuccessfulWrite)
      .recover { case _ => StorageNodeWritingResult.FailedWrite }
  }
}
