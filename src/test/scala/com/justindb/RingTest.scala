package com.justindb

import org.scalatest._
import com.justindb.{Ring, Node, Hash}

class RingTest extends FlatSpec with Matchers {

  "Ring" should "be empty after initialization" in {
    val ring = new Ring

    ring.underlying should have size 0
  }

  it should "allow to save Node with combined Hash" in {
    var ring = new Ring

    val node = Node(Key(value = "random-key"), underlyingActor = null)
    val nodeHash = Hash(500)
    ring = Ring(ring.underlying + ((nodeHash -> node)))

    ring.underlying should have size 1
  }

}