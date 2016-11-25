package justin.db

import java.util.UUID

import justin.vector_clocks.VectorClock

case class Data(id: UUID, value: String /*, vclock: VectorClock*/)
