package justin.db.vectorclocks

import justin.db.vectorclocks.VectorClockComparator.VectorClockRelation
import justin.db.versioning.VectorClockOps.stringAsId
import org.scalatest.{FlatSpec, Matchers}

class VectorClockComparatorTest extends FlatSpec with Matchers {

  behavior of "Vector Clock's comparator"

  private val comparator = new VectorClockComparator[String]

  /**
    * -------------------
    * CONFLICT scenarios |
    * -------------------
    */
  it should "pass conflict scenario nr 1" in {
    val baseVC     = "A:1, B:1"
    val comparedVC = "A:1, C:1"

    comparator.apply(baseVC, comparedVC) shouldBe VectorClockRelation.Conflict
  }

  it should "pass conflict scenario nr 2" in {
    val baseVC     = "A:2"
    val comparedVC = "A:1, B:1"

    comparator.apply(baseVC, comparedVC) shouldBe VectorClockRelation.Conflict
  }

  it should "pass conflict scenario nr 3" in {
    val baseVC     = "A:1"
    val comparedVC = "B:1"

    comparator.apply(baseVC, comparedVC) shouldBe VectorClockRelation.Conflict
  }

  it should "pass conflict scenario nr 4" in {
    val baseVC     = "A:1, B:1, C:2"
    val comparedVC = "A:1, B:2, C:1"

    comparator.apply(baseVC, comparedVC) shouldBe VectorClockRelation.Conflict
  }

  it should "pass conflict scenario nr 5" in {
    val baseVC     = "A:1, B:2"
    val comparedVC = "A:1, B:1, C:1"

    comparator.apply(baseVC, comparedVC) shouldBe VectorClockRelation.Conflict
  }

  /**
    * ----------------------
    * PREDECESSOR scenarios |
    * ----------------------
    */
  it should "pass predecessor scenario nr 1" in {
    val baseVC     = "A:2, B:1"
    val comparedVC = "A:1, B:1"

    comparator.apply(baseVC, comparedVC) shouldBe VectorClockRelation.Predecessor
  }

  it should "pass predecessor scenario nr 2" in {
    val baseVC     = "A:2"
    val comparedVC = "A:1"

    comparator.apply(baseVC, comparedVC) shouldBe VectorClockRelation.Predecessor
  }

  it should "pass predecessor scenario nr 3" in {
    val baseVC     = "A:1, B:1"
    val comparedVC = "A:1, B:1"

    comparator.apply(baseVC, comparedVC) shouldBe VectorClockRelation.Predecessor
  }

  /**
    * ----------------------
    * CONSEQUENT scenarios  |
    * ----------------------
    */
  it should "pass consequent scenario nr 1" in {
    val baseVC     = "A:1"
    val comparedVC = "A:2"

    comparator.apply(baseVC, comparedVC) shouldBe VectorClockRelation.Consequent
  }

  it should "pass consequent scenario nr 2" in {
    val baseVC     = "A:1, B:1"
    val comparedVC = "A:1, B:1, C:1"

    comparator.apply(baseVC, comparedVC) shouldBe VectorClockRelation.Consequent
  }

  it should "pass consequent scenario nr 3" in {
    val baseVC     = "A:1, B:1, C:2"
    val comparedVC = "A:1, B:1, C:3"

    comparator.apply(baseVC, comparedVC) shouldBe VectorClockRelation.Consequent
  }
}
