package justin.db

import java.util.UUID

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{FlatSpecLike, Matchers}
import justin.db.StorageNodeActorProtocol.{StorageNodeReadData, StorageNodeReadingResult}
import org.scalatest.concurrent.ScalaFutures

class RemoteDataReadingServiceTest extends TestKit(ActorSystem("test-system"))
  with FlatSpecLike
  with Matchers
  with ScalaFutures {

  behavior of "Remote Data Reading Service"

  it should "get info back that one of the value could be found and second one is obsolete" in {
    // given
    val service = new RemoteDataReadingService()(system.dispatcher)
    val id = UUID.randomUUID()
    val storageNotFoundActorRef = testActorRef(msgBack = StorageNodeReadingResult.NotFound)
    val storageFoundActorRef    = testActorRef(msgBack = StorageNodeReadingResult.Found(Data(id, "value")))
    val storageNodeRefs         = List(storageNotFoundActorRef, storageFoundActorRef).map(StorageNodeActorRef)

    // when
    val readingResult = service.apply(storageNodeRefs, id)

    // then
    whenReady(readingResult) { _ === List(StorageNodeReadingResult.NotFound, StorageNodeReadingResult.Found(Data(id, "value"))) }
  }

  it should "recover failed behavior of actor" in {
    // given
    val service = new RemoteDataReadingService()(system.dispatcher)
    val id = UUID.randomUUID()
    val storageActorRef = testActorRef(new Exception)
    val storageNodeRefs = List(StorageNodeActorRef(storageActorRef))
    // when
    val readingResult = service.apply(storageNodeRefs, id)

    // then
    whenReady(readingResult) { _ shouldBe List(StorageNodeReadingResult.FailedRead) }
  }

  private def testActorRef(msgBack: => Any) = {
    TestActorRef(new Actor {
      override def receive: Receive = {
        case StorageNodeReadData.Local(id) => sender() ! msgBack
      }
    })
  }
}
