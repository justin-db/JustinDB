package justin.httpapi

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

class BuildInfoRouter {

  def routes(buildInfoJson: String): Route = path("info") {
    get {
      complete(HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/json`), buildInfoJson)))
    }
  }
}
