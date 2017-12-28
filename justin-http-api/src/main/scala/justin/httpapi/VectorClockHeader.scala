package justin.httpapi

import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}
import justin.db.consistenthashing.NodeId
import justin.db.vectorclocks.VectorClock
import justin.db.versioning.NodeIdVectorClockBase64

import scala.util.Try

case class VectorClockHeaderException(msg: String) extends Exception(msg)

case class VectorClockHeader(vectorClock: VectorClock[NodeId]) extends ModeledCustomHeader[VectorClockHeader] {
  override def companion: ModeledCustomHeaderCompanion[VectorClockHeader] = VectorClockHeader

  override def value(): String = new NodeIdVectorClockBase64().encode(vectorClock) match {
    case scala.util.Success(vclock) => vclock
    case scala.util.Failure(_)      => throw VectorClockHeaderException("Couldn't encode vector clock of data")
  }

  override def renderInResponses(): Boolean = true
  override def renderInRequests(): Boolean = true
}

object VectorClockHeader extends ModeledCustomHeaderCompanion[VectorClockHeader] {
  override def name: String = "X-Vector-Clock"

  override def parse(value: String): Try[VectorClockHeader] = {
    new NodeIdVectorClockBase64()
      .decode(value)
      .map(VectorClockHeader(_))
  }

  def empty: VectorClockHeader = VectorClockHeader(VectorClock.apply[NodeId]())
}
