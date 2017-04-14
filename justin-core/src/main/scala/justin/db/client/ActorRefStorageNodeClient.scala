package justin.db.client

import java.util.UUID

import akka.pattern.ask
import akka.util.Timeout
import justin.db.Data
import justin.db.actors.StorageNodeActorRef
import justin.db.actors.protocol._
import justin.db.replica.{R, W}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ActorRefStorageNodeClient(private val storageNodeActor: StorageNodeActorRef)(implicit ex: ExecutionContext) extends StorageNodeClient {

  implicit val timeout = Timeout(5.seconds) // TODO: tune this value

  override def get(id: UUID, r: R): Future[GetValueResponse] = {
    (storageNodeActor.storageNodeActor ? Internal.ReadReplica(r, id)).mapTo[StorageNodeReadResponse].map {
      case StorageNodeFoundRead(data)      => GetValueResponse.Found(data)
      case StorageNodeConflictedRead(data) => GetValueResponse.Conflicts(data)
      case StorageNodeNotFoundRead(id)     => GetValueResponse.NotFound(id)
      case StorageNodeFailedRead(_)        => GetValueResponse.Failure(s"Couldn't read value with id ${id.toString}")
    } recover { case ex: Throwable         => GetValueResponse.Failure(s"Unsuccessful read of value with id ${id.toString}") }
  }

  override def write(data: Data, w: W): Future[WriteValueResponse] = {
    (storageNodeActor.storageNodeActor ? Internal.WriteReplica(w, data)).mapTo[StorageNodeWriteResponse].map {
      case StorageNodeSuccessfulWrite(_)    => WriteValueResponse.Success
      case StorageNodeConflictedWrite(_, _) => WriteValueResponse.Conflict
      case StorageNodeFailedWrite(id)       => WriteValueResponse.Failure(s"Couldn't write value with id ${id.toString}")
    } recover { case ex: Throwable          => WriteValueResponse.Failure(s"Unsuccessful write of value with id ${data.id.toString}") }
  }
}
