package justin.db

import java.util.UUID

import justin.db.replication.PreferenceList
import justin.db.versioning.NodeIdVectorClock
import justin.vector_clocks.VectorClock

case class Data(id: UUID, value: String, vclock: NodeIdVectorClock = VectorClock(), timestamp: Long = System.currentTimeMillis())

object Data {

  def updateVclock(data: Data, preferenceList: PreferenceList): Data = {
    val updatedVclock = preferenceList.list.foldLeft(data.vclock)(_ increase _)
    data.copy(vclock = updatedVclock)
  }
}
