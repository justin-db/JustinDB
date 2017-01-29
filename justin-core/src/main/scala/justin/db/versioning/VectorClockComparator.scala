package justin.db.versioning

import justin.db.versioning.VectorClockComparator.VectorClockRelation
import justin.vector_clocks.VectorClock

case class VCs2Compare[Id](baseVC: VectorClock[Id], potentiallyConsequentVC: VectorClock[Id])

// TODO: refactor (too much complexity)
class VectorClockComparator[Id] extends (VCs2Compare[Id] => VectorClockRelation) {
  override def apply(vcs: VCs2Compare[Id]): VectorClockRelation = {
    val vcKeys  = vcs.baseVC.keys
    val vc2Keys = vcs.potentiallyConsequentVC.keys

    val vc2ContainsAllKeysOfVc = !vcs.potentiallyConsequentVC.keys.forall(vcKeys.contains)
    val vcContainsAllKeysOfVc2 = !vcKeys.forall(vc2Keys.contains)

    val (counter1, counter2) = vc2Keys.foldLeft((0,0)) { (counter, vc2Key) =>
      val vc1Val = vcs.baseVC.get(vc2Key)
      val vc2Val = vcs.potentiallyConsequentVC.get(vc2Key)

      if(vc1Val.isEmpty || vc1Val.get.value == vc2Val.get.value) {
         counter
      } else {
        if(vc1Val.get.value > vc2Val.get.value) (counter._1 + 1, counter._2)
        else (counter._1, counter._2 + 1)
      }
    }

    if(isConflict(vc2ContainsAllKeysOfVc, vcContainsAllKeysOfVc2, counter1, counter2)) {
      VectorClockRelation.Conflict
    } else if(isConsequent(vc2ContainsAllKeysOfVc, counter1, counter2)) {
      VectorClockRelation.Consequent
    } else {
      VectorClockRelation.Predecessor
    }
  }

  private def isConflict(vc2ContainsAllKeysOfVc: Boolean, vcContainsAllKeysOfVc2: Boolean, counter1: Int, counter2: Int) = {
    (vc2ContainsAllKeysOfVc && vcContainsAllKeysOfVc2) || (vc2ContainsAllKeysOfVc && counter1 > 0) || (counter1 > 0 && counter2 > 0)
  }
  private def isConsequent(vc2ContainsAllKeysOfVc: Boolean, counter1: Int, counter2: Int) = {
    vc2ContainsAllKeysOfVc && counter1 >= 0 && counter2 >= 0 || !(counter1 >= 0 && counter2 == 0)
  }
}

object VectorClockComparator {

  sealed trait VectorClockRelation
  object VectorClockRelation {
    case object Predecessor extends VectorClockRelation
    case object Conflict    extends VectorClockRelation
    case object Consequent  extends VectorClockRelation
  }
}
