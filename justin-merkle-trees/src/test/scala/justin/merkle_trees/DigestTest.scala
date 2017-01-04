package justin.merkle_trees

import org.scalatest.{FlatSpec, Matchers}

class DigestTest extends FlatSpec with Matchers {

  behavior of "Digest"

  it should "define addition operator" in {
    val digest1 = Digest(Array[Byte](1,2,3))
    val digest2 = Digest(Array[Byte](4,5,6))

    val result = digest1 + digest2

    result.hash.deep shouldBe Array[Byte](1,2,3,4,5,6)
  }

  it should "define equality operator" in {
    val digest1 = Digest(Array[Byte](1,2,3))
    val digest2 = Digest(Array[Byte](1,2,3))

    val equal = digest1 == digest2

    equal shouldBe true
  }
}
