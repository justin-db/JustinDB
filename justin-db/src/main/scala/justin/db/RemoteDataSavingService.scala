package justin.db

import scala.concurrent.{ExecutionContext, Future}

class RemoteDataSavingService(implicit ec: ExecutionContext) {
  import akka.pattern.ask
  import akka.util.Timeout
  import scala.concurrent.duration._

  private implicit val timeout = Timeout(3.seconds) // TODO: tune this value

  def apply(storageNodeRefs: List[StorageNodeActorRef], data: Data): Future[List[StorageNodeWritingResult]] = {
    val msg = StorageNodeActor.PutLocalValue(data)
    Future.sequence(storageNodeRefs.map(putLocalValue(_, msg)))
  }

  private def putLocalValue(node: StorageNodeActorRef, msg: StorageNodeActor.PutLocalValue): Future[StorageNodeWritingResult] = {
    (node.storageNodeActor ? msg)
      .map {
        case StorageNodeActor.SuccessfulWrite   => StorageNodeWritingResult.SuccessfulWrite
        case StorageNodeActor.UnsuccessfulWrite => StorageNodeWritingResult.FailedWrite
      }.recover {
      case _ => StorageNodeWritingResult.FailedWrite
    }
  }
}
