package justin.httpapi

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class HealthCheckRouter {

  val routes: Route = path("health") {
    get { complete(StatusCodes.OK) }
  }
}
