package com.justindb

import org.scalatest._
import com.justindb.Ring

class RingTest extends FlatSpec with Matchers {

  "Ring" should "be empty after initialization" in {
    val ring = new Ring

    ring.underlying should have size 0
  }

}