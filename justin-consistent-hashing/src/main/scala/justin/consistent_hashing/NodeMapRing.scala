package justin.consistent_hashing

import justin.consistent_hashing.NodeMapRing.RingKey

case class NodeId(id: Int) extends AnyVal

case class NodeMapRing(private val ring: Map[RingKey, NodeId]) {
  def getByKey(key: RingKey): Option[NodeId] = ring.get(key)

  lazy val size: Int = ring.size

  lazy val nodesId: Set[NodeId] = ring.values.toSet

  lazy val swap: Map[NodeId, List[RingKey]] = {
    ring.groupBy { case (_, v) => v }
      .mapValues(_.keys.toList.sorted)
  }
}

object NodeMapRing {
  type RingKey = Int

  def apply(N: Int = 3, S: Int = 50): NodeMapRing = {
    val ring = for {
      id      <- 0 until N
      ringKey <- id until S by N
    } yield (ringKey, NodeId(id))

    NodeMapRing(ring.toMap)
  }
}
