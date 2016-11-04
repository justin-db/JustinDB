package justin.consistent_hashing

import justin.consistent_hashing.NodeMapRing.RingKey

case class NodeId(id: Int) extends AnyVal

// TODO: change def's to lazy vals
case class NodeMapRing(private val ring: Map[RingKey, NodeId]) {
  def getByKey(key: RingKey): Option[NodeId] = ring.get(key)
  def size: Int = ring.size
  def nodesId: Set[NodeId] = ring.values.toSet
  def swap: Map[NodeId, List[RingKey]] = {
    val initial = Map.empty[NodeId, List[RingKey]]
    ring.foldLeft(initial) { case (finalMap, (ringKey, nodeId)) =>
      val currentNodeVals = finalMap.getOrElse(nodeId, List.empty[RingKey])
      val finalVals = ringKey :: currentNodeVals
      finalMap ++ Map(nodeId -> finalVals.sorted)
    }
  }
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
