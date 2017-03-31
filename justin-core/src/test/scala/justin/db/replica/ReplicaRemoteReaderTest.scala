package justin.db.replica

import java.util.UUID

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import justin.db.Data
import justin.db.actors.StorageNodeActorRef
import justin.db.actors.protocol.{StorageNodeLocalRead, StorageNodeReadResponse}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpecLike, Matchers}

class ReplicaRemoteReaderTest extends TestKit(ActorSystem("test-system"))
  with FlatSpecLike
  with Matchers
  with ScalaFutures {

  behavior of "Replica Remote Reader"

  it should "get info back that one of the value could be found and second one is obsolete" in {
    // given
    val service = new ReplicaRemoteReader()(system.dispatcher)
    val id = UUID.randomUUID()
    val foundData = Data(id, "value")
    val storageNotFoundActorRef = testActorRef(msgBack = StorageNodeReadResponse.NotFound)
    val storageFoundActorRef    = testActorRef(msgBack = StorageNodeReadResponse.StorageNodeFoundRead(foundData))
    val storageNodeRefs         = List(storageNotFoundActorRef, storageFoundActorRef).map(StorageNodeActorRef)

    // when
    val readingResult = service.apply(storageNodeRefs, id)

    // then
    whenReady(readingResult) { _ shouldBe List(StorageNodeReadResponse.NotFound, StorageNodeReadResponse.StorageNodeFoundRead(foundData)) }
  }

  it should "recover failed behavior of actor" in {
    // given
    val service = new ReplicaRemoteReader()(system.dispatcher)
    val id = UUID.randomUUID()
    val storageActorRef = testActorRef(new Exception)
    val storageNodeRefs = List(StorageNodeActorRef(storageActorRef))

    // when
    val readingResult = service.apply(storageNodeRefs, id)

    // then
    whenReady(readingResult) { _ shouldBe List(StorageNodeReadResponse.FailedRead) }
  }

  private def testActorRef(msgBack: => Any) = {
    TestActorRef(new Actor {
      override def receive: Receive = {
        case StorageNodeLocalRead(id) => sender() ! msgBack
      }
    })
  }
}
