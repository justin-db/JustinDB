package justin.db.client

import java.util.UUID

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import justin.db.StorageNodeActorProtocol.{StorageNodeReadData, StorageNodeReadingResult, StorageNodeWriteData, StorageNodeWritingResult}
import justin.db.replication.{R, W}
import justin.db.{Data, StorageNodeActorRef}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class HttpStorageNodeClientTest extends TestKit(ActorSystem("test-system"))
  with FlatSpecLike
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll {

  behavior of "HTTP Storage Node Client"

  /**
    * GET part
    */
  it should "handle actor's \"Found\" message for asked data" in {
    // given
    val id       = UUID.randomUUID()
    val actorRef = getTestActorRef(msgBack = StorageNodeReadingResult.Found(Data(id, "value")))
    val client   = new HttpStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.get(id, R(1))

    // then
    whenReady(result) { _ shouldBe GetValueResponse.Found("value") }
  }

  it should "handle actor's \"NotFound\" message for asked data" in {
    // given
    val id       = UUID.randomUUID()
    val actorRef = getTestActorRef(msgBack = StorageNodeReadingResult.NotFound)
    val client   = new HttpStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.get(id, R(1))

    // then
    whenReady(result) { _ shouldBe GetValueResponse.NotFound }
  }

  it should "handle actor's \"FailedRead\" message for asked data" in {
    // given
    val id       = UUID.randomUUID()
    val actorRef = getTestActorRef(msgBack = StorageNodeReadingResult.FailedRead)
    val client   = new HttpStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.get(id, R(1))

    // then
    whenReady(result) { _ shouldBe GetValueResponse.Failure(s"[HttpStorageNodeClient] Couldn't read value with id ${id.toString}") }
  }

  it should "recover actor's reading behavior" in {
    // given
    val id       = UUID.randomUUID()
    val actorRef = getTestActorRef(msgBack = new Exception)
    val client   = new HttpStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.get(id, R(1))

    // then
    whenReady(result) { _ shouldBe GetValueResponse.Failure(s"[HttpStorageNodeClient] Couldn't read value with id ${id.toString}") }
  }

  /**
    * WRITE part
    */
  it should "handle actor's \"SuccessfulWrite\" message for data saving" in {
    // given
    val id       = UUID.randomUUID()
    val data     = Data(id, "value")
    val actorRef = writeTestActorRef(msgBack = StorageNodeWritingResult.SuccessfulWrite)
    val client   = new HttpStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.write(W(1), data)

    // then
    whenReady(result) { _ shouldBe WriteValueResponse.Success }
  }

  it should "handle actor's \"FailedWrite\" message for data saving" in {
    // given
    val id       = UUID.randomUUID()
    val data     = Data(id, "value")
    val actorRef = writeTestActorRef(msgBack = StorageNodeWritingResult.FailedWrite)
    val client   = new HttpStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.write(W(1), data)

    // then
    whenReady(result) { _ shouldBe WriteValueResponse.Failure(s"[HttpStorageNodeClient] Couldn't write data: $data") }
  }

  it should "recover actor's writing behavior" in {
    // given
    val id       = UUID.randomUUID()
    val data     = Data(id, "value")
    val actorRef = writeTestActorRef(msgBack = new Exception)
    val client   = new HttpStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.write(W(1), data)

    // then
    whenReady(result) { _ shouldBe WriteValueResponse.Failure(s"[HttpStorageNodeClient] Couldn't write data: $data") }
  }

  it should "handle actor's \"ConflictedWrite\" message for data saving" in {
    // given
    val id       = UUID.randomUUID()
    val data     = Data(id, "value")
    val actorRef = writeTestActorRef(msgBack = StorageNodeWritingResult.ConflictedWrite)
    val client   = new HttpStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.write(W(1), data)

    // then
    whenReady(result) { _ shouldBe WriteValueResponse.Conflict }
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  private def getTestActorRef(msgBack: => Any) = {
    TestActorRef(new Actor {
      override def receive: Receive = {
        case StorageNodeReadData.Replicated(r, id) => sender() ! msgBack
      }
    })
  }

  private def writeTestActorRef(msgBack: => Any) = {
    TestActorRef(new Actor {
      override def receive: Receive = {
        case StorageNodeWriteData.Replicate(w, data) => sender() ! msgBack
      }
    })
  }
}
