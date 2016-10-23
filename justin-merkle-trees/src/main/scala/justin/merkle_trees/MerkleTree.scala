package justin.merkle_trees

import scala.util.Try

sealed trait MerkleTree { def hash: Digest }
case class MerkleHashNode(hash: Digest, left: MerkleTree, right: MerkleTree) extends MerkleTree
case class MerkleLeaf(hash: Digest) extends MerkleTree

object MerkleTree {

  def unapply(data: Seq[Block])(implicit ev: MerkleDigest[Block]): Option[MerkleTree] = {
    def blockToLeaf(b: Block) = MerkleLeaf(ev.digest(b))

    val leafs = data.map(blockToLeaf)
    Try(buildTree(leafs)).toOption
  }

  private def buildTree(leafs: Seq[MerkleLeaf])(implicit ev: MerkleDigest[Block]): MerkleTree = {
    def merge(n1: MerkleTree, n2: MerkleTree) = MerkleHashNode(n1.hash + n2.hash, n1, n2)

    var trees: Seq[MerkleTree] = leafs

    while (trees.length > 1) {
      trees = trees.grouped(2)
        .map { case (n1 :: n2 :: Nil) => merge(n1, n2) }
        .toSeq
    }

    trees.head
  }
}
