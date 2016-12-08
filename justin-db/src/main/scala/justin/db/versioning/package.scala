package justin.db

import justin.vector_clocks.{Counter, VectorClock}

package object versioning {

  /**
    * Create Vector Clock from plain string eg. "A:1, B:1, C:1"
    */
  implicit class VectorClockOps(plain: String) {
    def toVectorClock[Id]: VectorClock[Id] = VectorClock.apply {
      plain.split(",").map { s =>
        val Array(key, value) = s.trim.split(":")
        (key.asInstanceOf[Id], Counter(value.toInt))
      }.toMap
    }
  }

  object VectorClockOps {
    implicit def string2VectorClock(s: String): VectorClock[String] = s.toVectorClock[String]
  }
}
