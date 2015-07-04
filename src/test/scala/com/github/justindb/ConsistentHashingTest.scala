package com.github.justindb

import com.github.justindb.common.Record
import com.github.justindb.consistent_hashing.ConsistentHashing
import org.scalatest._

class ConsistentHashingTest extends FlatSpec with Matchers {

  it should "find created records using Consistent Hashing Algorithm" in {

    ConsistentHashing.addNodes(
      realNodes = 5,
      virtualNodesPerNode = 2000
    )

    ConsistentHashing.addRecord(
      key = "aaaa",
      v = Record("first record")
    )
    ConsistentHashing.addRecord(
      key = "bbbb",
      v = Record("second record")
    )
    ConsistentHashing.addRecord(
      key = "cccc",
      v = Record(9999L)
    )

    ConsistentHashing.findRecord("aaaa").get.v should be("first record")
    ConsistentHashing.findRecord("cccc").get.v should be(9999L)
  }

}

