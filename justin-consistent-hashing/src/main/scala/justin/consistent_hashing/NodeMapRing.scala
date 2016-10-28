package justin.consistent_hashing

import justin.consistent_hashing.NodeMapRing.RingKey

case class NodeId(id: Int) extends AnyVal

case class NodeMapRing(private val ring: Map[RingKey, NodeId]) {
  def getByKey(key: RingKey): Option[NodeId] = ring.get(key)
  def size: Int = ring.size
}

object NodeMapRing {
  type RingKey = Int

  def apply(N: Int = 3, S: Int = 50): NodeMapRing = {
    val slices = (nodeId: NodeId) => nodeId.id until S by N
    val slice = (nodeId: NodeId) => slices(nodeId).map(ringKey => (ringKey, nodeId))
    val nodesIds = (0 until N).map(NodeId)

    val ring = nodesIds.flatMap(slice).toMap

    NodeMapRing(ring)
  }
}
