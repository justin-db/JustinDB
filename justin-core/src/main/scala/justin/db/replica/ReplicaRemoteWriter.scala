package justin.db.replica

import akka.pattern.ask
import akka.util.Timeout
import justin.db.Data
import justin.db.actors.StorageNodeActorProtocol.{StorageNodeWriteData, StorageNodeWritingResult}
import justin.db.actors.StorageNodeActorRef

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ReplicaRemoteWriter(implicit ec: ExecutionContext) {

  private implicit val timeout = Timeout(3.seconds) // TODO: tune this value

  def apply(storageNodeRefs: List[StorageNodeActorRef], data: Data): Future[List[StorageNodeWritingResult]] = {
    Future.sequence(storageNodeRefs.map(putLocalValue(_, data)))
  }

  private def putLocalValue(node: StorageNodeActorRef, data: Data): Future[StorageNodeWritingResult] = {
    (node.storageNodeActor ? StorageNodeWriteData.Local(data))
      .mapTo[StorageNodeWritingResult]
      .recover { case _ => StorageNodeWritingResult.FailedWrite }
  }
}
