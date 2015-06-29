package com.github.justindb.consistent_hashing

import scala.collection.immutable.{ TreeMap, SortedMap }
import scala.util.hashing.MurmurHash3

case class Node(id: Int, name: String)

object ConsistentHashing {

  var ring: SortedMap[Int, Node] = TreeMap.empty

  def hashFunc(key: String): Int = MurmurHash3.stringHash(key)

  def addNodes(realNodes: Int, virtualNodesPerNode: Int): Unit = {
    (1 to realNodes).foreach { i =>
      ConsistentHashing.add(Node(i, s"r=$i"), virtualNodesPerNode)
    }
  }

  private def add(node: Node, virtualNodesPerNode: Int): Unit = {
    (1 to virtualNodesPerNode).map { i =>
      ring = ring + ((hashFunc(node.name + i), node.copy(name = node.name + ":v=" + i.toString)))
    }
  }

  def get(entryKey: Any): Option[Int] = {
    if (ring.isEmpty)
      None
    else {
      val hash = hashFunc(entryKey.toString)
      val tailMap = ring.from(hash)

      val nodeKey = if (tailMap.isEmpty)
        ring.firstKey
      else
        tailMap.firstKey

      Some(nodeKey)
    }
  }
}