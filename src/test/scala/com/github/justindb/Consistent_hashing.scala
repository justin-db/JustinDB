package com.github.justindb

import com.github.justindb.consistent_hashing.{ LongRecord, TextRecord, Node, ConsistentHashing }
import org.scalatest._

class Consistent_hashing extends FlatSpec with Matchers {

  it should "find created records using Consistent Hashing Algorithm" in {

    ConsistentHashing.addNodes(
      realNodes = 5,
      virtualNodesPerNode = 2000
    )

    for {
      nodeId <- ConsistentHashing.getNodeId("aaaa")
    } yield {
      val node = ConsistentHashing.ring(nodeId)
    }

    Node.addRecord(
      key = "aaaa",
      v = TextRecord("first record")
    )
    Node.addRecord(
      key = "bbbb",
      v = TextRecord("second record")
    )
    Node.addRecord(
      key = "cccc",
      v = LongRecord(9999L)
    )

    Node.findRecord("aaaa").get should be(TextRecord("first record"))
    Node.findRecord("cccc").get should be(LongRecord(9999L))

  }

}

