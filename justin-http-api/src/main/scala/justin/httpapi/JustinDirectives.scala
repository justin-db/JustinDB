package justin.httpapi

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.optionalHeaderValueByType
import akka.http.scaladsl.server.Directives.provide

trait JustinDirectives {

  def withVectorClockHeader: Directive1[VectorClockHeader] = {
    optionalHeaderValueByType[VectorClockHeader]((): Unit).flatMap {
      case Some(header) => provide(header)
      case None         => provide(VectorClockHeader.empty)
    }
  }
}

object JustinDirectives extends JustinDirectives
