package justin.merkle_trees

object MTApp extends App {

  def digest(i: Byte*): Digest = Digest(i.toArray)


  /**
    *       1234
    *    12      34
    *  1    2  3    4
    */
  val t1 = MerkleHashNode(
    MerkleNodeId(0), digest(1,2,3,4),

    MerkleHashNode(MerkleNodeId(1), digest(1,2),
        MerkleLeaf(MerkleNodeId(2), digest(1)),
        MerkleLeaf(MerkleNodeId(3), digest(2))),

    MerkleHashNode(MerkleNodeId(4), digest(3,4),
        MerkleLeaf(MerkleNodeId(5), digest(3)),
        MerkleLeaf(MerkleNodeId(6), digest(4)))
  )
//  println("t1: " + t1)
//  println()

  /**
    *       1235
    *    12      35
    *  1    2  3    5
    */
  val t2 = MerkleHashNode(
    MerkleNodeId(0), digest(1,2,3,5),

    MerkleHashNode(MerkleNodeId(1), digest(1,2),
      MerkleLeaf(MerkleNodeId(2), digest(1)),
      MerkleLeaf(MerkleNodeId(3), digest(2))),

    MerkleHashNode(MerkleNodeId(4), digest(3,5),
      MerkleLeaf(MerkleNodeId(5), digest(3)),
      MerkleLeaf(MerkleNodeId(6), digest(5)))
  )

//  println("t2: " + t2)
//  println()
//
//  println("diff result: " + diff(RootEntry(t1.digest), t1))


  def getLevelEntries(id: MerkleNodeId, mt: MerkleTree): List[Entry] = {
    MerkleTree.findNode(id, mt).toList.map(mt => Entry(mt.nodeId, mt.digest))
  }

  val baseTree   = t1 // gives root entry and start whole process
  val targetTree = t2

  def diff(root: RootEntry) = {
    if(root.digest == targetTree.digest)
      RootsOk
    else {
      //
      getLevelEntries(MerkleNodeId(0), t1)
    }
  }
}

case class Entry(id: MerkleNodeId, digest: Digest)
case class RootEntry(digest: Digest)


sealed trait Diff
case object RootsOk extends Diff
