package com.github.justindb

import com.github.justindb.consistent_hashing.{ Node, LongRecord, TextRecord, ConsistentHashing }

import scala.util.Random

object Main extends App {

  ConsistentHashing.addNodes(
    realNodes = 5,
    virtualNodesPerNode = 2000
  )

  for {
    nodeId <- ConsistentHashing.getNodeId("aaaa")
  } yield {
    val node = ConsistentHashing.ring(nodeId)
    println(node)
    println(node.store)
  }

  Node.addRecord(
    key = "aaaa",
    v = TextRecord("pierwszy rekord w bazie")
  )
  Node.addRecord(
    key = "bbbb",
    v = TextRecord("drugi rekord w bazie")
  )
  Node.addRecord(
    key = "cccc",
    v = LongRecord(9999L)
  )

  assert(Node.findRecord("aaaa").get == TextRecord("pierwszy rekord w bazie"))
  assert(Node.findRecord("cccc").get == LongRecord(9999L))

}
