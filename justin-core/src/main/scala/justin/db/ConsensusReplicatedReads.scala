package justin.db

import justin.consistent_hashing.NodeId
import justin.db.ConsensusReplicatedReads.ConsensusSummary
import justin.db.StorageNodeActorProtocol.StorageNodeReadingResult
import justin.db.replication.R
import justin.db.versioning.VectorClockComparator
import justin.db.versioning.VectorClockComparator.VectorClockRelation

class ConsensusReplicatedReads {

  def reach(r: R): List[StorageNodeReadingResult] => ConsensusSummary = {
    reads =>

      lazy val allNotFoundReads = reads.forall(_ == StorageNodeReadingResult.NotFound)
      lazy val allFailedReads   = reads.forall(_ == StorageNodeReadingResult.FailedRead)
      lazy val onlyFoundReads   = reads.collect { case r: StorageNodeReadingResult.Found => r }

      if(allNotFoundReads) {
        ConsensusSummary.AllNotFound
      } else if(allFailedReads) {
        ConsensusSummary.AllFailed
      } else {
        (onlyFoundReads.size >= r.r, onlyFoundReads.size == 1, hasSameVC(onlyFoundReads), foundOnlyConsequent(onlyFoundReads)) match {
          case (true, true, _, _)                 => ConsensusSummary.Found(onlyFoundReads.head.data)
          case (true, false, true, _)             => ConsensusSummary.Found(onlyFoundReads.head.data)
          case (true, false, _, c) if c.size == 1 => ConsensusSummary.Consequent(c.head._1)
          case (true, false, _, _)                => ConsensusSummary.Conflicts(onlyFoundReads.map(_.data))
          case (false, _, _, _)                   => ConsensusSummary.NotEnoughFound
        }
      }
  }

  private def hasSameVC(onlyFoundReads: List[StorageNodeReadingResult.Found]) = onlyFoundReads.map(_.data.vclock).distinct.size == 1

  private def foundOnlyConsequent(onlyFoundReads: List[StorageNodeReadingResult.Found]) = {
    val vcComparator = new VectorClockComparator[NodeId]

    onlyFoundReads.flatMap { compared =>
      onlyFoundReads.filterNot(_ == compared)
        .map(base => (compared.data, vcComparator.apply(base.data.vclock, compared.data.vclock)))
        .groupBy { case (data, _) => data }
        .filter { case (_, l) => l.forall { case (_, relation) => relation == VectorClockRelation.Consequent }}
    }
  }
}

object ConsensusReplicatedReads {

  sealed trait ConsensusSummary
  object ConsensusSummary {
    case object AllNotFound extends ConsensusSummary
    case object AllFailed extends ConsensusSummary
    case class Conflicts(data: List[Data]) extends ConsensusSummary
    case object NotEnoughFound extends ConsensusSummary
    // this should be chosen when all replicas agreed on the same value
    case class Found(data: Data) extends ConsensusSummary
    // this should be chosen when not all replicas agreed on but one of it has consequent vector clock
    case class Consequent(data: Data) extends ConsensusSummary
  }
}
