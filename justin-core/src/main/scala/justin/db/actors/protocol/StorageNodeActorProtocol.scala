package justin.db.actors.protocol

import java.util.UUID

import justin.db.Data
import justin.db.replica.{R, W}

sealed trait StorageNodeReadData
object StorageNodeReadData {
  case class Local(id: UUID)            extends StorageNodeReadData
  case class Replicated(r: R, id: UUID) extends StorageNodeReadData
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

sealed trait StorageNodeWritingResult
case class StorageNodeSuccessfulWrite(id: UUID)            extends StorageNodeWritingResult
object StorageNodeWritingResult {
  case class FailedWrite(id: UUID)                         extends StorageNodeWritingResult
  case class ConflictedWrite(oldData: Data, newData: Data) extends StorageNodeWritingResult
}
