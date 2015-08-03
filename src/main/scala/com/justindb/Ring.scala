package com.justindb

import scala.collection.immutable.{ TreeMap, SortedMap }
import com.justindb.HashApi.Hash

case class Ring(underlying: SortedMap[Hash, Node] = TreeMap.empty) extends AnyVal {
  def size = underlying.size
}

object Ring {

  def addNode(ring: Ring, nodeHash: Hash, node: Node): Ring = Ring(ring.underlying + ((nodeHash, node)))

  def getNode(ring: Ring, hash: Hash): Option[Node] = {
    if(ring.underlying.isEmpty)
      None
    else {
      val tailMap = ring.underlying.from(hash)
      val nodeHash = if(tailMap.isEmpty)
        ring.underlying.firstKey
      else
        tailMap.firstKey

      Some(ring.underlying(nodeHash))
    }
  }

}