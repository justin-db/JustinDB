package justin.db.actors.protocol

import java.util.UUID

import justin.db.Data
import justin.db.replica.{R, W}

// ----- READ PART ----
// INPUT
sealed trait StorageNodeReadData
object StorageNodeReadData {
  case class Local(id: UUID)            extends StorageNodeReadData
  case class Replicated(r: R, id: UUID) extends StorageNodeReadData
}
// OUTPUT
sealed trait StorageNodeReadingResult
object StorageNodeReadingResult {
  case class Found(data: Data)           extends StorageNodeReadingResult
  case class Conflicts(data: List[Data]) extends StorageNodeReadingResult
  case object NotFound                   extends StorageNodeReadingResult
  case object FailedRead                 extends StorageNodeReadingResult
}
// ------

// ----- WRITE PART ----
// INPUT
sealed trait StorageNodeWriteData
case class StorageNodeWriteDataLocal(data: Data) extends StorageNodeWriteData
object StorageNodeWriteData {
  case class Replicate(w: W, data: Data) extends StorageNodeWriteData
}
// OUTPUT
sealed trait StorageNodeWritingResult
object StorageNodeWritingResult {
  case object SuccessfulWrite                              extends StorageNodeWritingResult
  case object FailedWrite                                  extends StorageNodeWritingResult
  case class ConflictedWrite(oldData: Data, newData: Data) extends StorageNodeWritingResult
}
// ------