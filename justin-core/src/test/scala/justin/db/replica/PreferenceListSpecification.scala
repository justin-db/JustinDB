package justin.db.replica

import justin.db.consistenthashing.Ring
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Properties}

class PreferenceListSpecification extends Properties("PreferenceList") {

  property("head of preference-list is initial partitionId") = {
    val ring = Ring.apply(nodesSize = 5, partitionsSize = 64)
    val n = N(3)
    val partitionIdGen = Gen.choose(0, ring.size-1)

    forAll(partitionIdGen) { basePartitionId: Int =>
      PreferenceList(basePartitionId, n, ring).right.get.primaryNodeId == ring.getNodeId(basePartitionId).get
    }
  }
}
