package justin.db.client

import java.util.UUID

import akka.pattern.ask
import akka.util.Timeout
import justin.db.StorageNodeActorProtocol._
import justin.db.replication.{R, W}
import justin.db.{Data, StorageNodeActorRef}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class HttpStorageNodeClient(private val storageNodeActor: StorageNodeActorRef)(implicit ex: ExecutionContext) extends StorageNodeClient {

  implicit val timeout = Timeout(5.seconds) // TODO: tune this value

  override def get(id: UUID, r: R): Future[GetValueResponse] = {
    lazy val errorMsg = s"[HttpStorageNodeClient] Couldn't read value with id ${id.toString}"

    (storageNodeActor.storageNodeActor ? StorageNodeReadData.Replicated(r, id)).mapTo[StorageNodeReadingResult].map {
      case StorageNodeReadingResult.Found(data)              => GetValueResponse.Found(data.value)
      case StorageNodeReadingResult.NotFound                 => GetValueResponse.NotFound
      case StorageNodeReadingResult.FailedRead               => GetValueResponse.Failure(errorMsg)
      case StorageNodeReadingResult.Conflicted(data1, data2) => GetValueResponse.Conflicted(data1, data2)
    }.recover { case _                                       => GetValueResponse.Failure(errorMsg) }
  }

  override def write(w: W, data: Data): Future[WriteValueResponse] = {
    lazy val errorMsg = s"[HttpStorageNodeClient] Couldn't write data: $data"

    (storageNodeActor.storageNodeActor ? StorageNodeWriteData.Replicate(w, data)).mapTo[StorageNodeWritingResult].map {
      case StorageNodeWritingResult.SuccessfulWrite => WriteValueResponse.Success
      case StorageNodeWritingResult.ConflictedWrite => WriteValueResponse.Conflict
      case StorageNodeWritingResult.FailedWrite     => WriteValueResponse.Failure(errorMsg)
    }.recover { case _                              => WriteValueResponse.Failure(errorMsg) }
  }
}
