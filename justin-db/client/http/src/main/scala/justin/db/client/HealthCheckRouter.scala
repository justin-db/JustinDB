package justin.db.client

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer

import scala.concurrent.ExecutionContext

class HealthCheckRouter(implicit ec: ExecutionContext, mat: Materializer) {

  val routes: Route = path("health") {
    get { complete(StatusCodes.OK) }
  }
}
