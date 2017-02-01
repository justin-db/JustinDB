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

      (allNotFoundReads, allFailedReads) match {
        case (true, _) => ConsensusSummary.AllNotFound
        case (_, true) => ConsensusSummary.AllFailed
        case _ =>
          (onlyFoundReads.size >= r.r, onlyFoundReads.size == 1, foundOnlyConsequent(onlyFoundReads)) match {
            case (true, true, _)                 => ConsensusSummary.Consequent(onlyFoundReads.head.data)
            case (true, false, c) if c.size == 1 => ConsensusSummary.Consequent(c.head._1)
            case (true, false, _)                => ConsensusSummary.Conflicts(onlyFoundReads.map(_.data))
            case (false, _, _)                   => ConsensusSummary.NotEnoughFound
          }
      }
  }

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
    case object NotEnoughFound extends ConsensusSummary
    case class Consequent(data: Data) extends ConsensusSummary
    case class Conflicts(data: List[Data]) extends ConsensusSummary
  }
}
