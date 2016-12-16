package justin.db

import java.util.UUID

import justin.consistent_hashing.NodeId
import justin.db.versioning.NodeIdVectorClock
import justin.vector_clocks.VectorClock

case class Data(id: UUID, value: String, vclock: NodeIdVectorClock = VectorClock(), timestamp: Long = System.currentTimeMillis())

object Data {

  def updateVclock(data: Data, preferenceList: List[NodeId]): Data = {
    val updatedVclock = preferenceList.foldLeft(data.vclock)(_ increase _)
    data.copy(vclock = updatedVclock)
  }
}
