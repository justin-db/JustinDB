package justin.db.replica.write

import justin.db.actors.protocol.{StorageNodeSuccessfulWrite, StorageNodeWriteResponse}
import justin.db.replica.W

class ReplicaWriteAgreement {

  // TODO: more cases should be taken into account e.g. what if all writes were failed or one of it is conflicted?
  def reach(w: W): List[StorageNodeWriteResponse] => WriteAgreement = {
    writes =>
      val okWrites = writes.collect { case ok: StorageNodeSuccessfulWrite => ok }.size
      if(okWrites >= w.w) WriteAgreement.Ok else WriteAgreement.NotEnoughWrites
  }
}
