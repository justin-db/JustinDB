package com.github.justindb

import akka.actor.ActorSystem
import scala.collection.immutable.{ TreeMap, SortedMap }
import scala.util.hashing.MurmurHash3

case class Node(id: Int, name: String)

object Main extends App {

  val realNodes = 5
  val virtualNodesPerNode = 2 // it's usually much higher that number of real nodes

  var ring: SortedMap[Int, Node] = TreeMap.empty

  def hashFunc(key: String): Int = MurmurHash3.stringHash(key)

  def add(node: Node): Unit = {
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

  //   example
  (1 to realNodes).foreach { i =>
    add(Node(i, s"r=$i"))
  }

  println(ring.toString)

  println(get(78))
  println(get("ala"))
  println(get(Node(1, "alalala")))
  println(get(34534534L))
}
