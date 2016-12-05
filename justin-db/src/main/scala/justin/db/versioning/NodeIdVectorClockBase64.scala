package justin.db.versioning

import java.nio.charset.StandardCharsets
import java.util.Base64

import justin.consistent_hashing.NodeId
import justin.db.versioning.DataVersioning.NodeIdVectorClock
import justin.vector_clocks.{Counter, VectorClock}
import spray.json._
import spray.json.DefaultJsonProtocol._

import scala.util.Try

class NodeIdVectorClockBase64 {

  def decode(vclock: NodeIdVectorClock): Try[String] = Try {
    val vcClockBytes = vclock.toList
      .map { case (nodeId, counter) => (nodeId.id.toString, counter.value) }
      .toJson
      .compactPrint
      .getBytes(StandardCharsets.UTF_8)

    Base64.getEncoder.encodeToString(vcClockBytes)
  }
}
