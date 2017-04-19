package justin.db.replica.read

import justin.db.Data


sealed trait ReadAgreement

object ReadAgreement {
  case object AllNotFound extends ReadAgreement
  case object AllFailed extends ReadAgreement
  case class Conflicts(data: List[Data]) extends ReadAgreement
  case object NotEnoughFound extends ReadAgreement
  // this should be chosen when all replicas agreed on the same value
  case class Found(data: Data) extends ReadAgreement
  // this should be chosen when not all replicas agreed on but one of it has consequent vector clock
  case class Consequent(data: Data) extends ReadAgreement
}
