package justin.merkle_trees

import org.scalatest.{FlatSpec, Matchers}

class MerkleTreeTest extends FlatSpec with Matchers {

  behavior of "Merkle Tree"

  it should "have the same top hash" in {
    val blocks: Seq[Block] = Seq(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](7,8,9),
      Array[Byte](10,11,12)
    )
    val blocks2: Seq[Block] = Seq(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](7,8,9),
      Array[Byte](10,11,12)
    )

    val digest1 = MerkleTree.unapply(blocks)(MerkleDigest.CRC32).get.digest
    val digest2 = MerkleTree.unapply(blocks2)(MerkleDigest.CRC32).get.digest

    digest1.hash.deep shouldBe digest2.hash.deep
  }

  it should "have a different top hash" in {
    val blocks: Seq[Block] = Seq(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](7,8,9),
      Array[Byte](10,11,12)
    )
    val blocks2: Seq[Block] = Seq(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](9,8,7),
      Array[Byte](12,11,10)
    )

    val digest1 = MerkleTree.unapply(blocks)(MerkleDigest.CRC32).get.digest
    val digest2 = MerkleTree.unapply(blocks2)(MerkleDigest.CRC32).get.digest

    digest1.hash.deep should not be digest2.hash.deep
  }

  it should "use an all-zeros value to complete the pair" in {
    val oddBlocks: Seq[Block] = Seq(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](7,8,9)
    )

    val sameBlocksWithZeroed: Seq[Block] = Seq(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](7,8,9),
      Array[Byte](0)
    )

    val digest1 = MerkleTree.unapply(oddBlocks)(MerkleDigest.CRC32).get.digest
    val digest2 = MerkleTree.unapply(sameBlocksWithZeroed)(MerkleDigest.CRC32).get.digest

    digest1.hash.deep shouldBe digest2.hash.deep
  }

  it should "create missed zeroed byte blocks if initial blocks has NOT size of multiplication of 2" in {
    val blocks = Array(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](7,8,9),
      Array[Byte](7,8,9),
      Array[Byte](7,8,9)
    )

    val expected =  Array(
      Array[Byte](0),
      Array[Byte](0),
      Array[Byte](0)
    )

    val zeroed = MerkleTree.zeroed(blocks)

    zeroed.deep shouldBe expected.deep
  }

  it should "NOT create missed zeroed bytes blocks if initial blocks has size of multiplication of 2" in {
    val blocks = Array(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](7,8,9),
      Array[Byte](10,11,12)
    )

    val zeroed = MerkleTree.zeroed(blocks)

    zeroed.deep shouldBe empty
  }

  it should "start indexing of Merkle Tree nodes from 0" in {
    val blocks = Array(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](7,8,9),
      Array[Byte](10,11,12)
    )

    val firstNodeId = MerkleTree.unapply(blocks)(MerkleDigest.CRC32).get.nodeId.id

    firstNodeId shouldBe 0
  }
}
