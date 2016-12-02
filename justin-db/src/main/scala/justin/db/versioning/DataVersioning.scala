package justin.db.versioning

import justin.consistent_hashing.NodeId
import justin.vector_clocks.VectorClock

object DataVersioning {
  type NodeIdVectorClock = VectorClock[NodeId]
}
