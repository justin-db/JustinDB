package justin.db.merkletrees

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

  it should "create missed zeroed byte blocks if initial blocks size is NOT power of two" in {
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

  it should "NOT create missed zeroed bytes blocks if initial blocks size is power of two" in {
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

  it should "index Merkle Tree nodes in binary fashion" in {
    val blocks = Array(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](7,8,9),
      Array[Byte](10,11,12)
    )

    val tree = MerkleTree.unapply(blocks)(MerkleDigest.CRC32).get

    /** we should expect such tree indexing
      *         0
      *     1      4
      *   2   3  5   6
      */
    tree.nodeId.id                                                                       shouldBe 0
    tree.asInstanceOf[MerkleHashNode].left.nodeId.id                                     shouldBe 1
    tree.asInstanceOf[MerkleHashNode].left.asInstanceOf[MerkleHashNode].left.nodeId.id   shouldBe 2
    tree.asInstanceOf[MerkleHashNode].left.asInstanceOf[MerkleHashNode].right.nodeId.id  shouldBe 3
    tree.asInstanceOf[MerkleHashNode].right.nodeId.id                                    shouldBe 4
    tree.asInstanceOf[MerkleHashNode].right.asInstanceOf[MerkleHashNode].left.nodeId.id  shouldBe 5
    tree.asInstanceOf[MerkleHashNode].right.asInstanceOf[MerkleHashNode].right.nodeId.id shouldBe 6
  }

  it should "find node with its id" in {
    val blocks = Array(
      Array[Byte](1,2,3),
      Array[Byte](4,5,6),
      Array[Byte](7,8,9),
      Array[Byte](10,11,12)
    )
    val tree = MerkleTree.unapply(blocks)(MerkleDigest.CRC32).get

    MerkleTree.findNode(MerkleNodeId(0), tree).get.nodeId shouldBe MerkleNodeId(0)
    MerkleTree.findNode(MerkleNodeId(1), tree).get.nodeId shouldBe MerkleNodeId(1)
    MerkleTree.findNode(MerkleNodeId(2), tree).get.nodeId shouldBe MerkleNodeId(2)
    MerkleTree.findNode(MerkleNodeId(3), tree).get.nodeId shouldBe MerkleNodeId(3)
    MerkleTree.findNode(MerkleNodeId(4), tree).get.nodeId shouldBe MerkleNodeId(4)
    MerkleTree.findNode(MerkleNodeId(5), tree).get.nodeId shouldBe MerkleNodeId(5)
    MerkleTree.findNode(MerkleNodeId(6), tree).get.nodeId shouldBe MerkleNodeId(6)

    MerkleTree.findNode(MerkleNodeId(-1), tree) should not be defined
    MerkleTree.findNode(MerkleNodeId(7), tree)  should not be defined
  }
}
