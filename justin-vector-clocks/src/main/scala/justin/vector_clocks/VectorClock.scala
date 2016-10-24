package justin.vector_clocks

import java.util.UUID

case class VectorId(uuid: UUID) extends AnyVal
case class Counter(value: Int) extends AnyVal

case class VectorClock(private val clock: Map[VectorId, Counter])
