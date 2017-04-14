package justin.db.client

import java.util.UUID

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import justin.consistent_hashing.NodeId
import justin.db.Data
import justin.db.actors.StorageNodeActorRef
import justin.db.actors.protocol._
import justin.db.replica.{R, W}
import justin.vector_clocks.{Counter, VectorClock}
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
    whenReady(result) { _ shouldBe GetValueResponse.Failure(s"Couldn't read value with id ${id.toString}") }
  }

  it should "handle actor's \"ConflictedRead\" message for asked data" in {
    // given
    val id       = UUID.randomUUID()
    val oldData = Data(
      id        = id,
      value     = "some value 1",
      vclock    = VectorClock[NodeId](Map(NodeId(1) -> Counter(3))),
      timestamp = System.currentTimeMillis()
    )
    val newData = Data(
      id        = id,
      value     = "some value 2",
      vclock    = VectorClock[NodeId](Map(NodeId(1) -> Counter(1))),
      timestamp = System.currentTimeMillis()
    )
    val actorRef = getTestActorRef(msgBack = StorageNodeConflictedRead(List(oldData, newData)))
    val client   = new ActorRefStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.get(id, R(1))

    // then
    whenReady(result) { _ shouldBe GetValueResponse.Conflicts(List(oldData, newData)) }
  }

  it should "recover actor's reading behavior" in {
    // given
    val id       = UUID.randomUUID()
    val actorRef = getTestActorRef(msgBack = new Exception)
    val client   = new ActorRefStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.get(id, R(1))

    // then
    whenReady(result) { _ shouldBe GetValueResponse.Failure(s"Unsuccessful read of value with id ${id.toString}") }
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
    whenReady(result) { _ shouldBe WriteValueResponse.Failure(s"Couldn't write value with id ${id.toString}") }
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
    whenReady(result) { _ shouldBe WriteValueResponse.Failure(s"Unsuccessful write of value with id ${id.toString}") }
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
