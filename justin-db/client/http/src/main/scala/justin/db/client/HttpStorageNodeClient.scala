package justin.db.client

import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import justin.db.StorageNode.{GetValue, PutValue}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class HttpStorageNodeClient(storageNodeActor: ActorRef)(implicit ex: ExecutionContext) extends StorageNodeClient {

  implicit val timeout = Timeout(5.seconds) // TODO: tune this value

  // TODO: make it fully non-blocking
  override def get(id: UUID, r: ReadFactor): Future[GetValueResponse] = {
    (storageNodeActor ? GetValue(id)).mapTo[Option[String]].map {
      case Some(value) => GetValueResponse.Found(value)
      case None        => GetValueResponse.NotFound
    }.recover { case _ => GetValueResponse.Failure(s"[Failure] Couldn't get value with id ${id.toString}") }
  }

  override def write(id: UUID, value: String, w: WriteFactor): Future[WriteValueResponse] = {
    (storageNodeActor ? PutValue(id, value)).mapTo[String].map {
      case "ack" => WriteValueResponse.Success
    }.recover { case _ => WriteValueResponse.Failure(s"[Failure] Couldn't store value $value with id ${id.toString}") }
  }
}
