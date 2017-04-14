package justin.http_api

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import justin.db.Data
import justin.db.client.{ActorRefStorageNodeClient, GetValueResponse, WriteValueResponse}
import justin.db.replica.W
import justin.http_api.Unmarshallers.UUIDUnmarshaller
import spray.json.DefaultJsonProtocol._
import JustinDirectives._
import justin.db.replica.{R, W}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object HttpRouter {
  import Unmarshallers.UuidFormat

  case class Result(value: String)
  implicit val valueFormat = jsonFormat1(Result)

  case class PutValue(id: UUID, value: String, w: Int)
  implicit val putValueFormat = jsonFormat3(PutValue)
}

class HttpRouter(client: ActorRefStorageNodeClient)(implicit ec: ExecutionContext, mat: Materializer) {
  import HttpRouter._

  def routes: Route = withVectorClockHeader { vClockHeader =>
    {
      (get & path("get") & pathEndOrSingleSlash & parameters('id.as(UUIDUnmarshaller), 'r.as[Int])) { (uuid, r) =>
        onComplete(client.get(uuid, R(r))) {
          case Success(GetValueResponse.Found(data))     => respondWithHeader(VectorClockHeader(data.vclock)) { complete(OK -> Result(data.value)) }
          case Success(GetValueResponse.Conflicts(data)) => complete(MultipleChoices -> Result(data.toString)) // TODO: better representation of multiple choices
          case Success(GetValueResponse.NotFound(id))    => complete(NotFound -> Result(s"Couldn't found value with id ${id.toString}"))
          case Success(GetValueResponse.Failure(err))    => complete(BadRequest -> Result(err))
          case Failure(ex)                               => complete(InternalServerError -> Result(ex.getMessage))
        }
      }
    } ~
    (post & path("put") & pathEndOrSingleSlash & entity(as[PutValue])) { putValue =>
      complete {
        client.write(Data(putValue.id, putValue.value, vClockHeader.vectorClock), W(putValue.w)).map[ToResponseMarshallable] {
          case WriteValueResponse.Success      => NoContent
          case WriteValueResponse.Conflict     => MultipleChoices -> Result("Multiple Choices")
          case WriteValueResponse.Failure(err) => BadRequest      -> Result(err)
        }
      }
    }
  }
}
