package justin.vector_clocks

case class Counter(value: Int) extends AnyVal {
  def addOne: Counter = this.copy(value = value + 1)
}

object Counter {
  def max(c1: Counter, c2: Counter): Counter = {
    Counter(scala.math.max(c1.value, c2.value))
  }
}
