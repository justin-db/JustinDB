package justin.db.client

import java.util.UUID

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.concurrent.ExecutionContext

object StorageNodeRouter {
  case class Result(value: String)
  implicit val valueFormat = jsonFormat1(Result)
}

class StorageNodeRouter(client: HttpStorageNodeClient)(implicit ec: ExecutionContext, mat: Materializer) {
  import StorageNodeRouter._

  def routes: Route = {
    // TODO: id should be mapped to real UUID type
    (get & path("get") & pathEndOrSingleSlash & parameters('id.as[String], 'r.as[Int])) { (id, r) =>
      complete {
        client.get(UUID.fromString(id), ReadFactor(r)).map[ToResponseMarshallable] {
          case GetValueResponse.Found(value)  => OK         -> Result(value)
          case GetValueResponse.NotFound      => NotFound   -> Result(s"Not found value with id $id")
          case GetValueResponse.Failure(err)  => BadRequest -> Result(err)
        }
      }
    }
  }
}
