package justin.merkle_trees

import scala.util.Try

sealed trait MerkleTree { def hash: Digest }
case class MerkleHashNode(hash: Digest, left: MerkleTree, right: MerkleTree) extends MerkleTree
case class MerkleLeaf(hash: Digest) extends MerkleTree

object MerkleTree {

  def unapply(data: Seq[Block])(implicit ev: MerkleDigest[Block]): Option[MerkleTree] = {
    val leafs = data.map(blockToLeaf)
    buildTree(leafs).toOption
  }

  private def blockToLeaf(b: Block)(implicit ev: MerkleDigest[Block]) = MerkleLeaf(ev.digest(b))

  def buildTree(leafs: Seq[MerkleLeaf])(implicit ev: MerkleDigest[Block]) = Try {
    var trees: Seq[MerkleTree] = leafs

    while (trees.length > 1) {
      trees = trees.grouped(2)
        .map(x => mergeTrees(x(0), x(1)))
        .toSeq
    }
    trees.head
  }

  private def mergeTrees(n1: MerkleTree, n2: MerkleTree) = MerkleHashNode(n1.hash + n2.hash, n1, n2)
}
