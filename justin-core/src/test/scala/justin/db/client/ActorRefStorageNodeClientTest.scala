package justin.db.client

import java.util.UUID

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import justin.db.replica.W
import justin.db.Data
import justin.db.actors.protocol._
import justin.db.actors.StorageNodeActorRef
import justin.db.replica.{R, W}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class ActorRefStorageNodeClientTest extends TestKit(ActorSystem("test-system"))
  with FlatSpecLike
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll {

  behavior of "ActorRef Storage Node Client"

  /**
    * GET part
    */
  it should "handle actor's \"Found\" message for asked data" in {
    // given
    val id       = UUID.randomUUID()
    val data     = Data(id, "value")
    val actorRef = getTestActorRef(msgBack = StorageNodeFoundRead(data))
    val client   = new ActorRefStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.get(id, R(1))

    // then
    whenReady(result) { _ shouldBe GetValueResponse.Found(data) }
  }

  it should "handle actor's \"NotFound\" message for asked data" in {
    // given
    val id       = UUID.randomUUID()
    val actorRef = getTestActorRef(msgBack = StorageNodeNotFoundRead(id))
    val client   = new ActorRefStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.get(id, R(1))

    // then
    whenReady(result) { _ shouldBe GetValueResponse.NotFound }
  }

  it should "handle actor's \"FailedRead\" message for asked data" in {
    // given
    val id       = UUID.randomUUID()
    val actorRef = getTestActorRef(msgBack = StorageNodeFailedRead(id))
    val client   = new ActorRefStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.get(id, R(1))

    // then
    whenReady(result) { _ shouldBe GetValueResponse.Failure(s"[HttpStorageNodeClient] Couldn't read value with id ${id.toString}") }
  }

  it should "recover actor's reading behavior" in {
    // given
    val id       = UUID.randomUUID()
    val actorRef = getTestActorRef(msgBack = new Exception)
    val client   = new ActorRefStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

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
    val actorRef = writeTestActorRef(msgBack = StorageNodeSuccessfulWrite(id))
    val client   = new ActorRefStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.write(data, W(1))

    // then
    whenReady(result) { _ shouldBe WriteValueResponse.Success }
  }

  it should "handle actor's \"FailedWrite\" message for data saving" in {
    // given
    val id       = UUID.randomUUID()
    val data     = Data(id, "value")
    val actorRef = writeTestActorRef(msgBack = StorageNodeFailedWrite(id))
    val client   = new ActorRefStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.write(data, W(1))

    // then
    whenReady(result) { _ shouldBe WriteValueResponse.Failure(s"[HttpStorageNodeClient] Couldn't write data: $data") }
  }

  it should "recover actor's writing behavior" in {
    // given
    val id       = UUID.randomUUID()
    val data     = Data(id, "value")
    val actorRef = writeTestActorRef(msgBack = new Exception)
    val client   = new ActorRefStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.write(data, W(1))

    // then
    whenReady(result) { _ shouldBe WriteValueResponse.Failure(s"[HttpStorageNodeClient] Couldn't write data: $data") }
  }

  it should "handle actor's \"ConflictedWrite\" message for data saving" in {
    // given
    val id       = UUID.randomUUID()
    val data     = Data(id, "value")
    val actorRef = writeTestActorRef(msgBack = StorageNodeConflictedWrite(data, data))
    val client   = new ActorRefStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.write(data, W(1))

    // then
    whenReady(result) { _ shouldBe WriteValueResponse.Conflict }
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  private def getTestActorRef(msgBack: => Any) = {
    TestActorRef(new Actor {
      override def receive: Receive = {
        case Internal.ReadReplica(r, id) => sender() ! msgBack
      }
    })
  }

  private def writeTestActorRef(msgBack: => Any) = {
    TestActorRef(new Actor {
      override def receive: Receive = {
        case Internal.WriteReplica(w, data) => sender() ! msgBack
      }
    })
  }
}
