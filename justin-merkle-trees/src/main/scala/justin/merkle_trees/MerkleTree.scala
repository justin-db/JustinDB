package justin.merkle_trees

case class HashNode(hash: String) extends AnyVal

sealed trait MerkleTree
case class MerkleHashNode(hash: HashNode, left: Option[MerkleTree], right: Option[MerkleTree]) extends MerkleTree
case class MerkleLeaf(data: Array[Byte]) extends MerkleTree
