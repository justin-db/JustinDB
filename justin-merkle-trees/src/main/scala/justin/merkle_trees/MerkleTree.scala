package justin.merkle_trees

import scala.util.Try

sealed trait MerkleTree {
  def nodeId: NodeId
  def digest: Digest
}
case class MerkleHashNode(nodeId: NodeId, digest: Digest, left: MerkleTree, right: MerkleTree) extends MerkleTree
case class MerkleLeaf(nodeId: NodeId, digest: Digest) extends MerkleTree

object MerkleTree {

  def unapply(blocks: Seq[Block])(implicit ev: MerkleDigest[Block]): Option[MerkleTree] = unapply(blocks.toArray)

  def unapply(blocks: Array[Block])(implicit ev: MerkleDigest[Block]): Option[MerkleTree] = {
    sealed trait TempMerkleTree { def digest: Digest }
    case class TempMerkleHashNode(digest: Digest, left: TempMerkleTree, right: TempMerkleTree) extends TempMerkleTree
    case class TempMerkleLeaf(digest: Digest) extends TempMerkleTree

    def blockToLeaf(b: Block)(implicit ev: MerkleDigest[Block]) = TempMerkleLeaf(ev.digest(b))

    def buildTree(blocks: Array[Block])(implicit ev: MerkleDigest[Block]) = Try {
      val leafs = blocks.map(blockToLeaf)
      var trees: Seq[TempMerkleTree] = leafs

      while (trees.length > 1) {
        trees = trees.grouped(2)
          .map(x => mergeTrees(x(0), x(1)))
          .toSeq
      }
      trees.head
    }

    def mergeTrees(n1: TempMerkleTree, n2: TempMerkleTree)(implicit ev: MerkleDigest[Block]) = {
      val mergedDigest = n1.digest + n2.digest
      val hash = ev.digest(mergedDigest.hash)
      TempMerkleHashNode(hash, n1, n2)
    }

    def toFinalForm(tmt: TempMerkleTree): MerkleTree = {
      var counter = -1

      def toMerkle(mt: TempMerkleTree): MerkleTree = {
        counter += 1
        mt match {
          case TempMerkleHashNode(digest, left, right) => MerkleHashNode(NodeId(counter), digest, toMerkle(left), toMerkle(right))
          case TempMerkleLeaf(digest)                  => MerkleLeaf(NodeId(counter), digest)
        }
      }
      toMerkle(tmt)
    }

    buildTree(blocks ++ zeroed(blocks))
      .toOption
      .map(toFinalForm)
  }

  def zeroed(blocks: Seq[Block]): Array[Array[Byte]] = {
    def zero(i: Int): Int = {
      val factor = 2
      var x = factor
      while(x < i) x *= factor
      x - i
    }
    Array.fill(zero(blocks.length))(Array[Byte](0))
  }
}
