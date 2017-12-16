package justin.db.storage

import java.util.UUID

case class JustinData(id: UUID, value: String, vclock: Base64, timestamp: Long)
