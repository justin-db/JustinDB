package justin.db

import justin.consistent_hashing.NodeId
import justin.db.ReplicaReadAgreement.ReadAgreement
import justin.db.StorageNodeActorProtocol.StorageNodeReadingResult
import justin.db.replication.R
import justin.db.versioning.VectorClockComparator
import justin.db.versioning.VectorClockComparator.VectorClockRelation

class ReplicaReadAgreement {

  def reach(r: R): List[StorageNodeReadingResult] => ReadAgreement = { reads =>
    if(areAllNotFound(reads)) {
      ReadAgreement.AllNotFound
    } else if(areAllFailed(reads)) {
      ReadAgreement.AllFailed
    } else {
      val onlyFoundReads = collectFound(reads)
      (onlyFoundReads.size >= r.r, onlyFoundReads.size == 1, hasSameVC(onlyFoundReads), foundOnlyConsequent(onlyFoundReads)) match {
        case (true, true, _, _)                 => ReadAgreement.Found(onlyFoundReads.head.data)
        case (true, false, true, _)             => ReadAgreement.Found(onlyFoundReads.head.data)
        case (true, false, _, c) if c.size == 1 => ReadAgreement.Consequent(c.head._1)
        case (true, false, _, _)                => ReadAgreement.Conflicts(onlyFoundReads.map(_.data))
        case (false, _, _, _)                   => ReadAgreement.NotEnoughFound
      }
    }
  }

  private def areAllNotFound(reads: List[StorageNodeReadingResult]) = reads.forall(_ == StorageNodeReadingResult.NotFound)

  private def areAllFailed(reads: List[StorageNodeReadingResult]) = reads.forall(_ == StorageNodeReadingResult.FailedRead)

  private def collectFound(reads: List[StorageNodeReadingResult]) = reads.collect { case r: StorageNodeReadingResult.Found => r }

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

object ReplicaReadAgreement {

  sealed trait ReadAgreement
  object ReadAgreement {
    case object AllNotFound extends ReadAgreement
    case object AllFailed extends ReadAgreement
    case class Conflicts(data: List[Data]) extends ReadAgreement
    case object NotEnoughFound extends ReadAgreement
    // this should be chosen when all replicas agreed on the same value
    case class Found(data: Data) extends ReadAgreement
    // this should be chosen when not all replicas agreed on but one of it has consequent vector clock
    case class Consequent(data: Data) extends ReadAgreement
  }
}
