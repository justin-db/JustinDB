package justin.db

import java.util.UUID

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import justin.db.StorageNodeActorProtocol.{StorageNodeWriteData, StorageNodeWritingResult}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpecLike, Matchers}

class ReplicaRemoteWriterTest extends TestKit(ActorSystem("test-system"))
  with FlatSpecLike
  with Matchers
  with ScalaFutures {

  behavior of "Remote Data Saving Service"

  it should "get info back that one of the saving is successful and second one has failed" in {
    // given
    val service = new ReplicaRemoteWriter()(system.dispatcher)
    val data = Data(id = UUID.randomUUID(), value = "exemplary-value")
    val storageSuccessfulActorRef = testActorRef(msgBack = StorageNodeWritingResult.SuccessfulWrite)
    val storageFailedActorRef     = testActorRef(msgBack = StorageNodeWritingResult.FailedWrite)
    val storageNodeRefs           = List(storageSuccessfulActorRef, storageFailedActorRef).map(StorageNodeActorRef)

    // when
    val writingResult = service.apply(storageNodeRefs, data)

    // then
    whenReady(writingResult) { _ shouldBe List(StorageNodeWritingResult.SuccessfulWrite, StorageNodeWritingResult.FailedWrite) }
  }

  it should "recover failed behavior of actor" in {
    // given
    val service = new ReplicaRemoteWriter()(system.dispatcher)
    val data = Data(id = UUID.randomUUID(), value = "exemplary-value")
    val storageActorRef = testActorRef(new Exception)
    val storageNodeRefs = List(StorageNodeActorRef(storageActorRef))

    // when
    val writingResult = service.apply(storageNodeRefs, data)

    // then
    whenReady(writingResult) { _ shouldBe List(StorageNodeWritingResult.FailedWrite) }
  }

  private def testActorRef(msgBack: => Any) = {
    TestActorRef(new Actor {
      override def receive: Receive = {
        case StorageNodeWriteData.Local(id) => sender() ! msgBack
      }
    })
  }
}
