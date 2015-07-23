package com.justindb

import org.scalatest._
import com.justindb.{Ring, Node}

class RingTest extends FlatSpec with Matchers {

  "Ring" should "be empty after initialization" in {
    val ring = new Ring

    ring.underlying should have size 0
  }

  it should "allow to save Node with combined Hash" in {
    var ring = new Ring

    val node = Node(Key(value = "random-key"), underlyingActor = null)
    val nodeHash = 500
    ring = Ring(ring.underlying + ((nodeHash -> node)))

    ring.underlying should have size 1
  }

  it should "be possible to find Node in Ring based on its Hash" in {
    var ring = new Ring

    val node = Node(Key(value = "random-key-1"), underlyingActor = null)
    val nodeHash = 100
    ring = Ring(ring.underlying + ((nodeHash -> node)))

    val node2 = Node(Key(value = "random-key-2"), underlyingActor = null)
    val nodeHash2 = 500
    ring = Ring(ring.underlying + ((nodeHash2 -> node2)))

    Ring.getNode(ring, nodeHash2).get shouldBe node2
    Ring.getNode(ring, nodeHash).get shouldBe node
  }

}