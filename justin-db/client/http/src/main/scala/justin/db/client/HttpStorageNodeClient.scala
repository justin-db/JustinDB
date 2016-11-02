package justin.db.client

import java.util.UUID
import akka.actor.ActorRef
import scala.concurrent.{ExecutionContext, Future}

// TODO
class HttpStorageNodeClient(storageNodeActor: ActorRef)(implicit ex: ExecutionContext) extends StorageNodeClient {
  override def get(id: UUID, r: ReadFactor): Future[GetValueResponse] = ???

  override def write(value: String, w: WriteFactor): Future[GetValueResponse] = ???
}
