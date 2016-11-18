package justin.db

import java.util.UUID

import justin.db.consistent_hashing.NodeId
import justin.db.replication.{R, W}

object StorageNodeActorProtocol {

  // read part
  sealed trait StorageNodeReadData
  object StorageNodeReadData {
    case class Local(id: UUID)            extends StorageNodeReadData
    case class Replicated(r: R, id: UUID) extends StorageNodeReadData
  }

  sealed trait StorageNodeReadingResult
  object StorageNodeReadingResult {
    case class Found(d: Data) extends StorageNodeReadingResult
    case object NotFound      extends StorageNodeReadingResult
    case object FailedRead    extends StorageNodeReadingResult
  }

  // write part
  sealed trait StorageNodeWriteData
  object StorageNodeWriteData {
    case class Local(data: Data)           extends StorageNodeWriteData
    case class Replicate(w: W, data: Data) extends StorageNodeWriteData
  }

  sealed trait StorageNodeWritingResult
  object StorageNodeWritingResult {
    case object SuccessfulWrite extends StorageNodeWritingResult
    case object FailedWrite     extends StorageNodeWritingResult
  }

  // cluster part
  case class RegisterNode(nodeId: NodeId)
}
