package justin.http_client

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

trait JustinDirectives {

  def withVectorClockHeader: Directive1[VectorClockHeader] = {
    optionalHeaderValueByType[VectorClockHeader]().flatMap {
      case Some(header) => provide(header)
      case None         => provide(VectorClockHeader.empty)
    }
  }
}

object JustinDirectives extends JustinDirectives
