package justin.db

import justin.db.StorageNodeActor.{StorageNodeWriteData, StorageNodeWritingResult}

import scala.concurrent.{ExecutionContext, Future}

class RemoteDataSavingService(implicit ec: ExecutionContext) {
  import akka.pattern.ask
  import akka.util.Timeout
  import scala.concurrent.duration._

  private implicit val timeout = Timeout(3.seconds) // TODO: tune this value

  def apply(storageNodeRefs: List[StorageNodeActorRef], data: Data): Future[List[StorageNodeWritingResult]] = {
    val msg = StorageNodeWriteData.Local(data)
    Future.sequence(storageNodeRefs.map(putLocalValue(_, msg)))
  }

  private def putLocalValue(node: StorageNodeActorRef, msg: StorageNodeWriteData.Local): Future[StorageNodeWritingResult] = {
    (node.storageNodeActor ? msg)
      .mapTo[StorageNodeWritingResult]
      .recover { case _ => StorageNodeWritingResult.FailedWrite }
  }
}
