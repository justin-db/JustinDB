package justin.db.client

import java.util.UUID

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import justin.db.Data
import justin.db.client.Unmarshallers.UUIDUnmarshaller
import justin.db.replication.{R, W}
import spray.json.DefaultJsonProtocol._

import scala.concurrent.ExecutionContext

object StorageNodeRouter {
  import Unmarshallers.UuidFormat

  case class Result(value: String)
  implicit val valueFormat = jsonFormat1(Result)

  case class PutValue(id: UUID, value: String, w: Int)
  implicit val putValueFormat = jsonFormat3(PutValue)
}

class StorageNodeRouter(client: HttpStorageNodeClient)(implicit ec: ExecutionContext, mat: Materializer) {
  import StorageNodeRouter._

  def routes: Route = {
    (get & path("get") & pathEndOrSingleSlash & parameters('id.as(UUIDUnmarshaller), 'r.as[Int])) { (uuid, r) =>
      complete {
        client.get(uuid, R(r)).map[ToResponseMarshallable] {
          case GetValueResponse.Found(value)  => OK         -> Result(value)
          case GetValueResponse.NotFound      => NotFound   -> Result(s"Not found value with id ${uuid.toString}")
          case GetValueResponse.Failure(err)  => BadRequest -> Result(err)
        }
      }
    } ~
      (post & path("put") & pathEndOrSingleSlash & entity(as[PutValue])) { putValue =>
        complete {
          client.write(W(putValue.w), Data(putValue.id, putValue.value)).map[ToResponseMarshallable] {
            case WriteValueResponse.Success      => NoContent
            case WriteValueResponse.Failure(err) => BadRequest -> Result(err)
          }
        }
      }
  }
}
