package justin.merkle_trees

import org.scalatest.{FlatSpec, Matchers}

class MerkleTreeTest extends FlatSpec with Matchers {

  behavior of "Merkle Tree"

  it should "have the same top hash" in {
    val data: Seq[Block] = Seq(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](7,8,9),
      Array[Byte](10,11,12)
    )
    val data2: Seq[Block] = Seq(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](7,8,9),
      Array[Byte](10,11,12)
    )

    val hash1 = MerkleTree.unapply(data)(MerkleDigest.CRC32).get.hash
    val hash2 = MerkleTree.unapply(data2)(MerkleDigest.CRC32).get.hash

    hash1.hash.deep shouldBe hash2.hash.deep
  }

  it should "have a different top hash" in {
    val data: Seq[Block] = Seq(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](7,8,9),
      Array[Byte](10,11,12)
    )
    val data2: Seq[Block] = Seq(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](9,8,7),
      Array[Byte](12,11,10)
    )

    val hash1 = MerkleTree.unapply(data)(MerkleDigest.CRC32).get.hash
    val hash2 = MerkleTree.unapply(data2)(MerkleDigest.CRC32).get.hash

    hash1.hash.deep should not be hash2.hash.deep
  }
}
