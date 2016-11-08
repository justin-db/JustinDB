package justin.db.replication

import justin.consistent_hashing.RingSize
import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop._

class PreferenceListSpecification extends Properties("PreferenceList") {

  property("head of preference-list is base partitionId") = {
    val ringSize = RingSize(64)
    val n = N(3)
    val paritionIdGen = Gen.choose(0, ringSize.size)

    forAll(paritionIdGen) { baseParitionId: Int =>
      BuildPreferenceList(baseParitionId, n, ringSize).head == baseParitionId
    }
  }
}
