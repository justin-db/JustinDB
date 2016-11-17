package justin.db.client

import java.util.UUID

import akka.pattern.ask
import akka.util.Timeout
import justin.db.StorageNodeActor.{GetValue, StorageNodeWriteData, StorageNodeWritingResult}
import justin.db.{Data, StorageNodeActorRef}
import justin.db.replication.{R, W}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class HttpStorageNodeClient(storageNodeActor: StorageNodeActorRef)(implicit ex: ExecutionContext) extends StorageNodeClient {

  implicit val timeout = Timeout(5.seconds) // TODO: tune this value

  override def get(id: UUID, r: R): Future[GetValueResponse] = {
    (storageNodeActor.storageNodeActor ? GetValue(id)).mapTo[Option[String]].map {
      case Some(value) => GetValueResponse.Found(value)
      case None        => GetValueResponse.NotFound
    }.recover { case _ => GetValueResponse.Failure(s"[Failure] Couldn't get value with id ${id.toString}") }
  }

  override def write(w: W, data: Data): Future[WriteValueResponse] = {
    lazy val errorMsg = s"[Failure] Couldn't store value ${data.value} with id ${data.id.toString}"

    (storageNodeActor.storageNodeActor ? StorageNodeWriteData.Replicate(w, data)).map {
      case StorageNodeWritingResult.SuccessfulWrite => WriteValueResponse.Success
      case StorageNodeWritingResult.FailedWrite     => WriteValueResponse.Failure(errorMsg)
    }.recover { case _                              => WriteValueResponse.Failure(errorMsg) }
  }
}
