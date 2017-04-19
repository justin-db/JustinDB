package justin.db.replica.write

sealed trait WriteAgreement
object WriteAgreement {
  case object NotEnoughWrites extends WriteAgreement
  case object Ok extends WriteAgreement
}
