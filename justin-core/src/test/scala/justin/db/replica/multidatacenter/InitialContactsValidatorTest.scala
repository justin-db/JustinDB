package justin.db.replica.multidatacenter

import akka.actor.{Actor, ActorPath, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import justin.db.actors.StorageNodeActorRef
import justin.db.actors.protocol.MultiDataCenterContacts
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class InitialContactsValidatorTest extends TestKit(ActorSystem("test-system"))
  with FlatSpecLike
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll {

  behavior of "Initial Contacts Validator"

  it should "validate request for non-empty parsable input and pass it forward to actor" in {
    // GIVEN
    val contacts = List(
      "akka.tcp://justin-dc0@host1:2552/system/receptionist",
      "akka.tcp://justin-dc0@host2:2552/system/receptionist",
      "akka.tcp://justin-dc0@host2:2552/system/receptionist"
    )
    val actor = StorageNodeActorRef(TestActorRef(new Actor {
      override def receive: Receive = {
        case MultiDataCenterContacts(initialContacts) =>
          initialContacts shouldBe Set(ActorPath.fromString(contacts.head), ActorPath.fromString(contacts.tail.head))
      }
    }))
    val validator = new InitialContactsValidator(actor)

    // WHEN
    val response = validator.apply(contacts)

    // THEN
    response shouldBe InitialContactsValidator.OK
  }

  it should "reject request empty list of contacts" in {
    // GIVEN
    val contacts  = List.empty[String]
    val actor     = StorageNodeActorRef(Actor.noSender)
    val validator = new InitialContactsValidator(actor)

    // WHEN
    val response = validator.apply(contacts)

    // THEN
    response shouldBe InitialContactsValidator.NotValid
  }

  it should "reject request non-empty list if one of the address is not parsable" in {
    // GIVEN
    val contacts  = List(
      "akka.tcp://justin-dc0@host1:2552/system/receptionist",
      "non-parsable-address"
    )
    val actor     = StorageNodeActorRef(Actor.noSender)
    val validator = new InitialContactsValidator(actor)

    // WHEN
    val response = validator.apply(contacts)

    // THEN
    response shouldBe InitialContactsValidator.NotValid
  }
}
