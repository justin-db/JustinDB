package com.github.justindb

import java.util.{ TreeMap => JTreeMap }
import java.util.{ SortedMap => JSortedMap }
import akka.actor.ActorSystem
import scala.util.hashing.MurmurHash3

case class Node(name: String)

object Main extends App {

  val realNodes = 5
  val virtualNodesPerNode = 1 // it's usually much higher that number of real nodes

  var ring: JSortedMap[Int, Node] = new JTreeMap[Int, Node]()

  def hashFunc(key: String): Int = MurmurHash3.stringHash(key)

  def add(node: Node): Unit = {
    (1 to virtualNodesPerNode).map { i =>
      ring.put(hashFunc(node.name + i), node)
    }
  }

  def get(entryKey: Any): Option[Int] = {
    if (ring.isEmpty)
      None
    else {
      val hash = hashFunc(entryKey.toString)
      val tailMap = ring.tailMap(hash)

      val nodeKey = if (tailMap.isEmpty)
        ring.firstKey()
      else
        tailMap.firstKey()

      Some(nodeKey)
    }
  }

//   example
  (1 to realNodes).foreach { i =>
      add(Node(s"Node_nr_$i"))
  }

  println(ring.toString)

  println(get(78).map { })
  println(get("ala"))
  println(get(Node("alalala")))
  println(get(34534534L))
}
