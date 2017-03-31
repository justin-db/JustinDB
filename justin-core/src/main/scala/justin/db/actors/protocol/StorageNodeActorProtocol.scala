package justin.db.actors.protocol

import java.util.UUID

import justin.db.Data
import justin.db.replica.{R, W}

sealed trait StorageNodeReadRequest
object StorageNodeReadRequest {
  case class Local(id: UUID)            extends StorageNodeReadRequest
  case class Replicated(r: R, id: UUID) extends StorageNodeReadRequest
}
sealed trait StorageNodeReadingResult
object StorageNodeReadingResult {
  case class Found(data: Data)           extends StorageNodeReadingResult
  case class Conflicts(data: List[Data]) extends StorageNodeReadingResult
  case object NotFound                   extends StorageNodeReadingResult
  case object FailedRead                 extends StorageNodeReadingResult
}

sealed trait StorageNodeWriteData
case class StorageNodeWriteDataLocal(data: Data) extends StorageNodeWriteData
object StorageNodeWriteData {
  case class Replicate(w: W, data: Data) extends StorageNodeWriteData // Is this ever wired between remotes Actors?
}

sealed trait StorageNodeWriteResponse
case class StorageNodeSuccessfulWrite(id: UUID)                     extends StorageNodeWriteResponse
case class StorageNodeFailedWrite(id: UUID)                         extends StorageNodeWriteResponse
case class StorageNodeConflictedWrite(oldData: Data, newData: Data) extends StorageNodeWriteResponse
