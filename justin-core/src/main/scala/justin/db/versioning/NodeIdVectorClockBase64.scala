package justin.db.versioning

import java.nio.charset.{Charset, StandardCharsets}
import java.util.Base64

import justin.db.consistenthashing.NodeId
import justin.db.vectorclocks.{Counter, VectorClock}
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.util.Try

object NodeIdVectorClockBase64 {
  val charset: Charset = StandardCharsets.UTF_8
}

class NodeIdVectorClockBase64 {
  import NodeIdVectorClockBase64._

  def encode(vclock: VectorClock[NodeId]): Try[String] = Try {
    val vcClockBytes = vclock.toList
      .map { case (nodeId, counter) => (nodeId.id.toString, counter.value) }
      .toJson
      .compactPrint
      .getBytes(charset)

    Base64.getEncoder.encodeToString(vcClockBytes)
  }

  def decode(base64: String): Try[VectorClock[NodeId]] = Try {
    val decodedMap = new String(Base64.getDecoder.decode(base64), charset)
      .parseJson.convertTo[List[(String, Int)]]
      .map { case (k, v) => (NodeId(k.toInt), Counter(v))}
      .toMap

    VectorClock.apply(decodedMap)
  }
}
