package justin.db

import java.util.UUID

import justin.db.replication.PreferenceList
import justin.db.storage.JustinData
import justin.db.versioning.{NodeIdVectorClock, NodeIdVectorClockBase64}
import justin.vector_clocks.VectorClock
import scala.language.implicitConversions

case class Data(id: UUID, value: String, vclock: NodeIdVectorClock = VectorClock(), timestamp: Long = System.currentTimeMillis())

object Data {

  def updateVclock(data: Data, preferenceList: PreferenceList): Data = {
    val nodeIds = preferenceList.primaryNodeId :: preferenceList.replicasNodeId
    data.copy(vclock = nodeIds.foldLeft(data.vclock)(_ increase _))
  }

  implicit def toInternal(data: Data): JustinData = {
    val encodedVClock = new NodeIdVectorClockBase64().encode(data.vclock).get // TODO: check if encodeding of VClock is possible (make it typesafe)
    JustinData(data.id, data.value, encodedVClock, data.timestamp)
  }

  implicit def fromInternal(justinData: JustinData): Data = {
    val decodedVClock = new NodeIdVectorClockBase64().decode(justinData.vclock).get // TODO: check if decodeing of VClock is possible (make it typesafe)
    Data(justinData.id, justinData.value, decodedVClock, justinData.timestamp)
  }
}
