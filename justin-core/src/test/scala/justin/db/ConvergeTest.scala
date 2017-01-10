package justin.db

import org.scalatest.{FlatSpec, Matchers}

class ConvergeTest extends FlatSpec with Matchers {

  behavior of "converge"

  it should "converge tuple" in {
    // given
    val tuple = (1, List(2,3,4,5))

    // when
    val result = justin.db.converge(tuple)

    // then
    result shouldBe List(1,2,3,4,5)
  }
}
