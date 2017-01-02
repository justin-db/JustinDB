package justin.merkle_trees

import scala.util.Try

sealed trait MerkleTree { def digest: Digest }
case class MerkleHashNode(digest: Digest, left: MerkleTree, right: MerkleTree) extends MerkleTree
case class MerkleLeaf(digest: Digest) extends MerkleTree

object MerkleTree {

  def unapply(blocks: Seq[Block])(implicit ev: MerkleDigest[Block]): Option[MerkleTree] = {
    val leafs = blocks.map(blockToLeaf)
    buildTree(leafs).toOption
  }

  private def blockToLeaf(b: Block)(implicit ev: MerkleDigest[Block]) = MerkleLeaf(ev.digest(b))

  private def buildTree(leafs: Seq[MerkleLeaf])(implicit ev: MerkleDigest[Block]) = Try {
    var trees: Seq[MerkleTree] = leafs

    while (trees.length > 1) {
      trees = trees.grouped(2)
        .map(x => mergeTrees(x(0), x(1)))
        .toSeq
    }
    trees.head
  }

  private def mergeTrees(n1: MerkleTree, n2: MerkleTree)(implicit ev: MerkleDigest[Block]) = {
    val mergedDigest = n1.digest + n2.digest
    val hash = ev.digest(mergedDigest.hash)
    MerkleHashNode(hash, n1, n2)
  }
}
