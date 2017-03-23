package justin.db

import java.util.UUID

package object storage {

  type RingPartitionId = Int
  type Base64 = String

  case class JustinData(id: UUID, value: String, vclock: Base64, timestamp: Long)
}
