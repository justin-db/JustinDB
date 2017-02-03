package justin.db

import justin.db.ConsensusReplicatedWrites.ConsensusSummary
import justin.db.StorageNodeActorProtocol.StorageNodeWritingResult
import justin.db.replication.W

class ConsensusReplicatedWrites {

  // TODO: more cases should be taken into account e.g. what if all writes were failed or one of it is conflicted?
  def reach(w: W): List[StorageNodeWritingResult] => ConsensusSummary = {
    writes =>
      val okWrites = writes.count(_ == StorageNodeWritingResult.SuccessfulWrite)
      if(okWrites >= w.w) ConsensusSummary.Ok else ConsensusSummary.NotEnoughWrites
  }
}

object ConsensusReplicatedWrites {

  sealed trait ConsensusSummary
  object ConsensusSummary {
    case object NotEnoughWrites extends ConsensusSummary
    case object Ok extends ConsensusSummary
  }
}
