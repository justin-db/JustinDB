package com.justindb

import org.scalatest._
import com.justindb.{Ring, Node}

class RingTest extends FlatSpec with Matchers {

  "Ring" should "be empty after initialization" in {
    new Ring().underlying should have size 0
  }

  it should "get None when Ring is empty" in {
    Ring.getNode(new Ring, hash = "100") shouldBe None
  }

  it should "allow to save Node with combined Hash" in {
    val node = Node(Key(value = "random-key"), underlyingActor = null)
    val nodeHash = "500"

    val ring = Ring.addNode(new Ring, nodeHash, node)

    ring.underlying should have size 1
  }

  it should "get proper Node based on different hashes" in {
    var ring = new Ring

    val node = Node(Key(value = "random-key-1"), underlyingActor = null)
    val nodeHash = "100"
    ring = Ring.addNode(ring, nodeHash, node)

    val node2 = Node(Key(value = "random-key-2"), underlyingActor = null)
    val nodeHash2 = "500"
    ring = Ring.addNode(ring, nodeHash2, node2)

    Ring.getNode(ring, hash = "-1").get shouldBe node
    Ring.getNode(ring, hash = "0").get shouldBe node
    Ring.getNode(ring, hash = "99").get shouldBe node
    Ring.getNode(ring, hash = "100").get shouldBe node
    Ring.getNode(ring, hash = "101").get shouldBe node2
    Ring.getNode(ring, hash = "300").get shouldBe node2
    Ring.getNode(ring, hash = "499").get shouldBe node2
    Ring.getNode(ring, hash = "500").get shouldBe node2
    Ring.getNode(ring, hash = "550").get shouldBe node
    Ring.getNode(ring, hash = "9000").get shouldBe node
  }

}