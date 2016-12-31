package justin.db

import justin.db.StorageNodeActorProtocol.StorageNodeWritingResult
import justin.db.replication.W

object ReachConsensusReplicatedWrites {

  def apply(w: W): List[StorageNodeWritingResult] => StorageNodeWritingResult = {
    writes =>
      val okWrites = writes.count(_ == StorageNodeWritingResult.SuccessfulWrite)

      okWrites >= w.w match {
        case true  => StorageNodeWritingResult.SuccessfulWrite
        case false => StorageNodeWritingResult.FailedWrite
      }
  }
}
