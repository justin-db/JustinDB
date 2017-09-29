package justin.db

import java.util.UUID

import justin.db.replica.PreferenceList
import justin.db.storage.JustinData
import justin.db.vectorclocks.VectorClock
import justin.db.versioning.{NodeIdVectorClock, NodeIdVectorClockBase64}

import scala.language.implicitConversions

case class Data(id: UUID, value: String, vclock: NodeIdVectorClock = VectorClock(), timestamp: Long = System.currentTimeMillis())

object Data {

  def updateVclock(data: Data, preferenceList: PreferenceList): Data = {
    val nodeIds = preferenceList.all
    data.copy(vclock = nodeIds.foldLeft(data.vclock)(_ increase _))
  }

  implicit def toInternal(data: Data): JustinData = {
    val encodedVClock = new NodeIdVectorClockBase64().encode(data.vclock).get // TODO: check if encoding of VClock is possible (make it typesafe)
    JustinData(data.id, data.value, encodedVClock, data.timestamp)
  }

  implicit def fromInternal(justinData: JustinData): Data = {
    val decodedVClock = new NodeIdVectorClockBase64().decode(justinData.vclock).get // TODO: check if decoding of VClock is possible (make it typesafe)
    Data(justinData.id, justinData.value, decodedVClock, justinData.timestamp)
  }
}
