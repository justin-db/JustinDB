package justin.crdts

/**
  * CRDT - the grow-only set
  */
case class AddElement[A](a: A)

case class GOSet[A](values: Set[A]) {

  def contains(elem: A): Boolean = values.contains(elem)

  def update(addElement: AddElement[A]): GOSet[A] = GOSet(values + addElement.a)

  def merge(other: GOSet[A]): GOSet[A] = GOSet(values ++ other.values)
}
