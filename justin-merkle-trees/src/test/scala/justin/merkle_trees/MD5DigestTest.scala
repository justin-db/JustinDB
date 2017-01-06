package justin.merkle_trees

import java.security.MessageDigest

import org.scalatest.{FlatSpec, Matchers}

class MD5DigestTest extends FlatSpec with Matchers {

  it should "digest block of data with MD5 algorithm" in {
    // given
    val block: Block = Array[Byte](10, 11, 18, 127, 0, -128)

    // when
    val digest = MerkleDigest.MD5.digest(block)

    // then
    digest.hash.deep shouldBe MessageDigest.getInstance("MD5").digest(block)
  }
}
