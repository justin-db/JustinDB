package justin.db.versioning

import justin.db.versioning.VectorClockComparator.VectorClockRelation
import justin.vector_clocks.VectorClock

case class VCs2Compare[Id](baseVC: VectorClock[Id], potentiallyConsequentVC: VectorClock[Id])

class VectorClockComparator[Id] extends (VCs2Compare[Id] => VectorClockRelation) {
  override def apply(vcs: VCs2Compare[Id]): VectorClockRelation = {
    println("---")
    val vcKeys  = vcs.baseVC.keys
    val vc2Keys = vcs.potentiallyConsequentVC.keys

    val vc1_hasNotDefined: Boolean = !vc2Keys.forall(vcKeys.contains)
    val vc2_hasNotDefined: Boolean = !vcKeys.forall(vc2Keys.contains)

    val (c1, c2) = vc2Keys.foldLeft((0,0)) { (c, vc2Key) =>
      val vc1Val = vcs.baseVC.get(vc2Key)
      val vc2Val = vcs.potentiallyConsequentVC.get(vc2Key)

      if(vc1Val.isEmpty) {
        c
      } else {
        if(vc1Val.get.value == vc2Val.get.value) c
        else if(vc1Val.get.value > vc2Val.get.value) (c._1 + 1, c._2)
        else (c._1, c._2 + 1)
      }
    }

    lazy val isConflicted = (vc1_hasNotDefined && vc2_hasNotDefined) || (vc1_hasNotDefined && c1 > 0) || (c1 > 0 && c2 > 0)
    lazy val isPredecessor = c1 >= 0 && c2 == 0

    if(isConflicted)
      VectorClockRelation.Conflict

    else if(vc1_hasNotDefined && c1 >= 0 && c2 >= 0) VectorClockRelation.Consequent
    else if(isPredecessor)                      VectorClockRelation.Predecessor
    else                                            {println("lat one "); VectorClockRelation.Consequent }
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
