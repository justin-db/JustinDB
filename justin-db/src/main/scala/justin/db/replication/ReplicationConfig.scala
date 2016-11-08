package justin.db.replication

case class ReplicationConfig(n: N, r: R, w: W)

/**
  * the number of nodes to replicate to
  */
case class N(n: Int) extends AnyVal

/**
  * the number of nodes read from before returning
  */
case class R(r: Int) extends AnyVal

/**
  * the number of nodes written to before considered successful
  */
case class W(w: Int) extends AnyVal
