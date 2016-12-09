package justin.db.versioning

import justin.db.versioning.VectorClockComparator.VectorClockRelation
import org.scalatest.{FlatSpec, Matchers}
import justin.db.versioning.VectorClockOps._

class VectorClockComparatorTest extends FlatSpec with Matchers {

  behavior of "Vector Clock's comparator"

  private val comparator = new VectorClockComparator[String]

  /**
    * -------------------
    * CONFLICT scenarios |
    * -------------------
    */
  it should "c1" in {
    val vcs = VCs2Compare(
      baseVC                  = "A:1, B:1",
      potentiallyConsequentVC = "A:1, C:1"
    )
    comparator.apply(vcs) shouldBe VectorClockRelation.Conflict
  }

  it should "c2" in {
    val vcs4 = VCs2Compare(
      baseVC                  = "A:2",
      potentiallyConsequentVC = "A:1, B:1"
    )
    comparator.apply(vcs4) shouldBe VectorClockRelation.Conflict
  }

  it should "c3" in {
    val vcs = VCs2Compare(
      baseVC                  = "A:1",
      potentiallyConsequentVC = "B:1"
    )
    comparator.apply(vcs) shouldBe VectorClockRelation.Conflict
  }

  it should "c4" in {
    val vcs = VCs2Compare(
        baseVC                  = "A:1, B:1, C:2",
        potentiallyConsequentVC = "A:1, B:2, C:1"
    )
    comparator.apply(vcs) shouldBe VectorClockRelation.Conflict
  }

  it should "c5" in {
    val vcs = VCs2Compare(
      baseVC                  = "A:1, B:2",
      potentiallyConsequentVC = "A:1, B:1, C:1"
    )
    comparator.apply(vcs) shouldBe VectorClockRelation.Conflict
  }


  /**
    * ----------------------
    * PREDECESSOR scenarios |
    * ----------------------
    */
  it should "p1" in {
    val vcs = VCs2Compare(
      baseVC                  = "A:2, B:1",
      potentiallyConsequentVC = "A:1, B:1"
    )
    comparator.apply(vcs) shouldBe VectorClockRelation.Predecessor
  }

  it should "p2" in {
    val vcs6 = VCs2Compare(
      baseVC                  = "A:2",
      potentiallyConsequentVC = "A:1"
    )
    comparator.apply(vcs6) shouldBe VectorClockRelation.Predecessor
  }

  it should "p3" in {
    val vcs7 = VCs2Compare(
      baseVC                  = "A:1, B:1",
      potentiallyConsequentVC = "A:1, B:1"
    )
    comparator.apply(vcs7) shouldBe VectorClockRelation.Predecessor
  }

  /**
    * ----------------------
    * CONSEQUENT scenarios  |
    * ----------------------
    */
  it should "cs1" in {
    val vcs2 = VCs2Compare(
      baseVC                  = "A:1",
      potentiallyConsequentVC = "A:2"
    )
    comparator.apply(vcs2) shouldBe VectorClockRelation.Consequent
  }

  it should "cs2" in {
    val vcs2 = VCs2Compare(
      baseVC                  = "A:1, B:1",
      potentiallyConsequentVC = "A:1, B:1, C:1"
    )
    comparator.apply(vcs2) shouldBe VectorClockRelation.Consequent
  }

  it should "cs3" in {
    val vcs5 = VCs2Compare(
      baseVC                  = "A:1, B:1, C:2",
      potentiallyConsequentVC = "A:1, B:1, C:3"
    )
    comparator.apply(vcs5) shouldBe VectorClockRelation.Consequent
  }
}
