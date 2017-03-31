package justin.db.replica

import java.util.UUID

import akka.pattern.ask
import akka.util.Timeout
import justin.db.actors.protocol.{StorageNodeReadRequest, StorageNodeReadingResult}
import justin.db.actors.StorageNodeActorRef

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ReplicaRemoteReader(implicit ec: ExecutionContext) {

  private implicit val timeout = Timeout(3.seconds) // TODO: tune this value

  def apply(storageNodeRefs: List[StorageNodeActorRef], id: UUID): Future[List[StorageNodeReadingResult]] = {
    Future.sequence(storageNodeRefs.map(getValue(_, id)))
  }

  private def getValue(node: StorageNodeActorRef, id: UUID): Future[StorageNodeReadingResult] = {
    (node.storageNodeActor ? StorageNodeReadRequest.Local(id))
      .mapTo[StorageNodeReadingResult]
      .recover { case _ => StorageNodeReadingResult.FailedRead }
  }
}
