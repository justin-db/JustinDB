package justin.http_api

import java.util.UUID

import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer
import spray.json.{JsString, JsValue, JsonFormat, _}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object Unmarshallers {

  implicit val UuidFormat = new JsonFormat[UUID] {
    override def read(json: JsValue): UUID = {
      json match {
        case JsString(uuid) => Try(UUID.fromString(uuid)) match {
          case Success(parsedUuid) => parsedUuid
          case Failure(_)          => deserializationError("UUID could not be created from given string")
        }
        case _ => deserializationError("UUID could not be converted to UUID object.")
      }
    }
    override def write(obj: UUID): JsValue = JsString(obj.toString)
  }

  object UUIDUnmarshaller extends FromStringUnmarshaller[UUID] {
    override def apply(value: String)(implicit ec: ExecutionContext, materializer: Materializer): Future[UUID] = {
      Future.apply(UUID.fromString(value))
    }
  }
}
