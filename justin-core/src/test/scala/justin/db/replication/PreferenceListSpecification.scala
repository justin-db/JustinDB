package justin.db.replication

import justin.consistent_hashing.Ring
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Properties}

class PreferenceListSpecification extends Properties("PreferenceList") {

  property("head of preference-list is initial partitionId") = {
    val ring = Ring.apply(nodesSize = 5, partitionsSize = 64)
    val n = N(3)
    val partitionIdGen = Gen.choose(0, ring.size-1)

    forAll(partitionIdGen) { basePartitionId: Int =>
      PreferenceList(basePartitionId, n, ring).head == ring.getNodeId(basePartitionId).get
    }
  }

  property("size of preference-list is always the same as configured number of replicas") = {
    val ring = Ring.apply(nodesSize = 5, partitionsSize = 64)
    val basePartitionId = 0
    val replicaNrGen = Gen.choose(1, 1000)

    forAll(replicaNrGen) { n: Int =>
      PreferenceList(basePartitionId, N(n), ring).size == n
    }
  }
}
