package justin.db

import scala.language.implicitConversions

package object vectorclocks {

//  type NodeIdVectorClock = VectorClock[NodeId]

  /**
    * Create Vector Clock from plain string eg. "A:1, B:1, C:1"
    */
  implicit class VectorClockOps(plain: String) {
    def toVectorClock[Id](implicit string2Id: String => Id): VectorClock[Id] = VectorClock.apply {
      plain.split(",").map { s =>
        val Array(key, value) = s.trim.split(":")
        (string2Id(key), Counter(value.toInt))
      }.toMap
    }
  }

  object VectorClockOps {
    implicit def stringAsId(s: String): VectorClock[String] = s.toVectorClock[String]
    implicit def intAsId(s: String): VectorClock[Int]       = s.toVectorClock[Int](_.toInt)
//    implicit def nodeIdAsId(s: String): VectorClock[NodeId] = s.toVectorClock[NodeId](s => NodeId(s.toInt))
  }
}
