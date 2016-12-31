package justin.db

import justin.db.StorageNodeActorProtocol.StorageNodeReadingResult
import justin.db.replication.R

object ReachConsensusReplicatedReads {

  def apply(r: R): List[StorageNodeReadingResult] => StorageNodeReadingResult = {
    reads =>
      val onlyFoundReads = reads.collect { case r: StorageNodeReadingResult.Found => r }
      val onlyFailedReads = reads.forall(_ == StorageNodeReadingResult.FailedRead)

      (onlyFoundReads.size >= r.r, onlyFoundReads.headOption, onlyFailedReads) match {
        case (true, Some(exemplary), _) => exemplary
        case (_, _, true)               => StorageNodeReadingResult.FailedRead
        case _                          => StorageNodeReadingResult.NotFound
      }
  }
}
