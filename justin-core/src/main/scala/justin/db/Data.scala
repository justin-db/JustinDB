package justin.db

import java.util.UUID

import justin.db.replication.PreferenceList
import justin.db.storage.JustinData
import justin.db.versioning.{NodeIdVectorClock, NodeIdVectorClockBase64}
import justin.vector_clocks.VectorClock

case class Data(id: UUID, value: String, vclock: NodeIdVectorClock = VectorClock(), timestamp: Long = System.currentTimeMillis())

object Data {

  def updateVclock(data: Data, preferenceList: PreferenceList): Data = {
    val nodeIds = preferenceList.primaryNodeId :: preferenceList.replicasNodeId
    data.copy(vclock = nodeIds.foldLeft(data.vclock)(_ increase _))
  }

  // TODO: check if encodeding of VClock is possible (make it typesafe)
  implicit def toInternal(data: Data): JustinData = JustinData(data.id, data.value, new NodeIdVectorClockBase64().encode(data.vclock).get, data.timestamp)

  // TODO: check if decodeing of VClock is possible (make it typesafe)
  implicit def fromInternal(justinData: JustinData): Data = Data(justinData.id, justinData.value, new NodeIdVectorClockBase64().decode(justinData.vclock).get, justinData.timestamp)
}
