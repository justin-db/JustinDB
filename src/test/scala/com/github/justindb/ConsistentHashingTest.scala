package com.github.justindb

import com.github.justindb.common.{ LongRecord, TextRecord }
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
      v = TextRecord("first record")
    )
    ConsistentHashing.addRecord(
      key = "bbbb",
      v = TextRecord("second record")
    )
    ConsistentHashing.addRecord(
      key = "cccc",
      v = LongRecord(9999L)
    )

    ConsistentHashing.findRecord("aaaa").get should be(TextRecord("first record"))
    ConsistentHashing.findRecord("cccc").get should be(LongRecord(9999L))
  }

}

