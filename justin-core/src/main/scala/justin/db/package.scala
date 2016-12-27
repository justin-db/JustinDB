package justin

package object db {
  def converge[T](result: (T, List[T])): List[T] = result._1 :: result._2
}
