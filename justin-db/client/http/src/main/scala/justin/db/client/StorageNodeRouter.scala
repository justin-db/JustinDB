package justin.db.client

import java.util.UUID

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer

import scala.concurrent.ExecutionContext

class StorageNodeRouter(client: HttpStorageNodeClient)(implicit ec: ExecutionContext, mat: Materializer) {

  def routes: Route = {
    // TODO: id should be mapped to real UUID type
    (get & path("get") & pathEndOrSingleSlash & parameters('id.as[String], 'r.as[Int])) { (id, r) =>
      complete {
        client.get(UUID.fromString(id), ReadFactor(r)).map[ToResponseMarshallable] {
          case GetValueResponse.Found(value)  => ???
          case GetValueResponse.NotFound      => ???
          case GetValueResponse.Failure(err)  => ???
        }
      }
    }
  }
}
