package justin.db.replica.write

import akka.pattern.ask
import akka.util.Timeout
import justin.db.Data
import justin.db.actors.StorageNodeActorRef
import justin.db.actors.protocol.{StorageNodeFailedWrite, StorageNodeWriteDataLocal, StorageNodeWriteResponse}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ReplicaRemoteWriter(implicit ec: ExecutionContext) {

  private implicit val timeout = Timeout(3.seconds) // TODO: tune this value

  def apply(storageNodeRefs: List[StorageNodeActorRef], data: Data): Future[List[StorageNodeWriteResponse]] = {
    Future.sequence(storageNodeRefs.map(putLocalValue(_, data)))
  }

  private def putLocalValue(node: StorageNodeActorRef, data: Data): Future[StorageNodeWriteResponse] = {
    (node.storageNodeActor ? StorageNodeWriteDataLocal(data))
      .mapTo[StorageNodeWriteResponse]
      .recover { case _ => StorageNodeFailedWrite(data.id) }
  }
}
