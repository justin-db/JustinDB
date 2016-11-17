package justin.db

import java.util.UUID

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
    case StorageNodeWriteData.Local(id, value)        =>
      localSaving.apply(id, value)
    case StorageNodeWriteData.Replicate(w, id, value) =>
      val basePartitionId = new UUID2RingPartitionId(ring).apply(id)
      val preferenceList  = PreferenceList(basePartitionId, n, ring)

      val localWriteNodeOpt   = preferenceList.find(_ == nodeId)
      val remoteWriteNodeRefs = preferenceList.filterNot(_ == nodeId)
        .distinct
        .flatMap(clusterMembers.get)

      val writesResultsFt = localWriteNodeOpt match {
        case Some(_) => localSaving(id, value).zip(remoteSaving(remoteWriteNodeRefs, id, value)).map { case (a, b) => a :: b }
        case None    => remoteSaving(remoteWriteNodeRefs, id, value)
      }
      val successfulWritesFt = writesResultsFt.map(_.filter(_ == StorageNodeWritingResult.SuccessfulWrite))

      successfulWritesFt.map(ok =>
        if(ok.size >= w.w)
          StorageNodeWritingResult.SuccessfulWrite
        else
          StorageNodeWritingResult.FailedWrite
      )
  }
}

sealed trait StorageNodeWriteData
object StorageNodeWriteData {
  case class Local(id: UUID, value: String)           extends StorageNodeWriteData
  case class Replicate(w: W, id: UUID, value: String) extends StorageNodeWriteData
}

sealed trait StorageNodeWritingResult
object StorageNodeWritingResult {
  case object SuccessfulWrite extends StorageNodeWritingResult
  case object FailedWrite     extends StorageNodeWritingResult
}

class LocalDataSavingService(storage: PluggableStorage)(implicit ec: ExecutionContext) {
  def apply(id: UUID, value: String): Future[StorageNodeWritingResult] = {
    storage.put(id.toString, value)
      .map(_ => StorageNodeWritingResult.SuccessfulWrite)
      .recover { case _ => StorageNodeWritingResult.FailedWrite }
  }
}

class RemoteDataSavingService(implicit ec: ExecutionContext) {
  import akka.pattern.ask
  import akka.util.Timeout
  import scala.concurrent.duration._

  private implicit val timeout = Timeout(3.seconds) // TODO: tune this value

  def apply(storageNodeRefs: List[StorageNodeActorRef], id: UUID, value: String): Future[List[StorageNodeWritingResult]] = {
    val msg = StorageNodeActor.PutLocalValue(id, value)
    Future.sequence(storageNodeRefs.map(putLocalValue(_, msg)))
  }

  private def putLocalValue(node: StorageNodeActorRef, msg: StorageNodeActor.PutLocalValue): Future[StorageNodeWritingResult] = {
    (node.storageNodeActor ? msg)
      .map(_ => StorageNodeWritingResult.SuccessfulWrite)
      .recover { case _ => StorageNodeWritingResult.FailedWrite }
  }
}
