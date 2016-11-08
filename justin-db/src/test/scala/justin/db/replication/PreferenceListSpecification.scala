package justin.db.replication

import justin.consistent_hashing.RingSize
import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop._

class PreferenceListSpecification extends Properties("PreferenceList") {

  property("head of preference-list is initial partitionId") = {
    val ringSize = RingSize(64)
    val n = N(3)
    val partitionIdGen = Gen.choose(0, ringSize.size-1)

    forAll(partitionIdGen) { basePartitionId: Int =>
      BuildPreferenceList(basePartitionId, n, ringSize).head == basePartitionId
    }
  }

  property("size of preference-list is always the same as configured number of replicas") = {
    val ringSize = RingSize(64)
    val basePartitionId = 0
    val replicaNrGen = Gen.choose(0, 1000)

    forAll(replicaNrGen) { n: Int =>
      BuildPreferenceList(basePartitionId, N(n), ringSize).size == n
    }
  }
}
