package justin.vector_clocks

import org.scalatest.{FlatSpec, Matchers}

class CounterTest extends FlatSpec with Matchers {

  behavior of "Counter"

  it should "create new Counter with increased by one when invoking \"addOne\" method on it" in {
    val counter = Counter(0)

    val increased = counter.addOne

    increased shouldBe Counter(1)
  }

  it should "choose Counter with bigger value" in {
    val counter1 = Counter(0)
    val counter2 = Counter(100)

    val max = Counter.max(counter1, counter2)

    max shouldBe counter2
  }
}
