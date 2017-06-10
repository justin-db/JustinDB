package justin.httpapi

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import justin.db.Data
import justin.db.client.{ActorRefStorageNodeClient, GetValueResponse, WriteValueResponse}
import justin.db.replica.{R, W}
import justin.db.storage.Base64
import justin.db.versioning.NodeIdVectorClockBase64
import justin.httpapi.JustinDirectives._
import justin.httpapi.Unmarshallers.UUIDUnmarshaller
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object HttpRouter {
  import Unmarshallers.UuidFormat

  case class Result(value: String)
  implicit val valueFormat: RootJsonFormat[Result] = jsonFormat1(Result)

  case class ConflictedData(id: String, value: String, vclock: Base64)
  implicit val conflictedDataFormat: RootJsonFormat[ConflictedData] = jsonFormat3(ConflictedData)

  case class PutValue(id: UUID, value: String, w: Int)
  implicit val putValueFormat: RootJsonFormat[PutValue] = jsonFormat3(PutValue)
}

class HttpRouter(client: ActorRefStorageNodeClient)(implicit ec: ExecutionContext, mat: Materializer) {
  import HttpRouter._

  private[this] def transformConflictedData(data: Data) = {
    val vcBase64 = new NodeIdVectorClockBase64().encode(data.vclock).get
    ConflictedData(data.id.toString, data.value, vcBase64)
  }

  def routes: Route = withVectorClockHeader { vClockHeader =>
    {
      (get & path("get") & pathEndOrSingleSlash & parameters('id.as(UUIDUnmarshaller), 'r.as[Int])) { (uuid, r) =>
        onComplete(client.get(uuid, R(r))) {
          case Success(GetValueResponse.Found(data))     => respondWithHeader(VectorClockHeader(data.vclock)) { complete(OK -> Result(data.value)) }
          case Success(GetValueResponse.Conflicts(data)) => complete(MultipleChoices -> data.map(transformConflictedData))
          case Success(GetValueResponse.NotFound(id))    => complete(NotFound -> Result(s"Couldn't found value with id ${id.toString}"))
          case Success(GetValueResponse.Failure(err))    => complete(BadRequest -> Result(err))
          case Failure(ex)                               => complete(InternalServerError -> Result(ex.getMessage))
        }
      }
    } ~
    (post & path("put") & pathEndOrSingleSlash & entity(as[PutValue])) { putValue =>
      complete {
        client.write(Data(putValue.id, putValue.value, vClockHeader.vectorClock), W(putValue.w)).map[ToResponseMarshallable] {
          case WriteValueResponse.Success(id)  => NoContent
          case WriteValueResponse.Conflict     => MultipleChoices -> Result("Multiple Choices")
          case WriteValueResponse.Failure(err) => BadRequest      -> Result(err)
        }
      }
    }
  }
}
