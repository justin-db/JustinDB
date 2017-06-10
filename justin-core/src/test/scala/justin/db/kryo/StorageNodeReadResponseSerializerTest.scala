package justin.db.kryo

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.UUID

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import justin.db.Data
import justin.db.actors.protocol.{StorageNodeConflictedRead, StorageNodeFailedRead, StorageNodeFoundRead, StorageNodeNotFoundRead}
import justin.db.consistenthashing.NodeId
import justin.db.vectorclocks.{Counter, VectorClock}
import org.scalatest.{FlatSpec, Matchers}

class StorageNodeReadResponseSerializerTest extends FlatSpec with Matchers {

  behavior of "StorageNodeReadResponse Serializer"

  it should "serialize/deserialize StorageNodeFoundRead" in {
    // kryo init
    val kryo = new Kryo()
    kryo.register(classOf[StorageNodeFoundRead], StorageNodeReadResponseSerializer)

    // object
    val data = Data(
      id        = UUID.randomUUID(),
      value     = "some value",
      vclock    = VectorClock[NodeId](Map(NodeId(1) -> Counter(3))),
      timestamp = System.currentTimeMillis()
    )
    val serializedData = StorageNodeFoundRead(data)

    // serialization
    val bos    = new ByteArrayOutputStream()
    val output = new Output(bos)
    val _      = kryo.writeObject(output, serializedData)
    output.flush()

    // deserialization
    val bis              = new ByteArrayInputStream(bos.toByteArray)
    val input            = new Input(bis)
    val deserializedData = kryo.readObject(input, classOf[StorageNodeFoundRead])

    serializedData shouldBe deserializedData
  }

  it should "serialize/deserialize StorageNodeNotFoundRead" in {
    // kryo init
    val kryo = new Kryo()
    kryo.register(classOf[StorageNodeNotFoundRead], StorageNodeReadResponseSerializer)

    // object
    val serializedData = StorageNodeNotFoundRead(UUID.randomUUID())

    // serialization
    val bos    = new ByteArrayOutputStream()
    val output = new Output(bos)
    val _      = kryo.writeObject(output, serializedData)
    output.flush()

    // deserialization
    val bis              = new ByteArrayInputStream(bos.toByteArray)
    val input            = new Input(bis)
    val deserializedData = kryo.readObject(input, classOf[StorageNodeNotFoundRead])

    serializedData shouldBe deserializedData
  }

  it should "serialize/deserialize StorageNodeFailedRead" in {
    // kryo init
    val kryo = new Kryo()
    kryo.register(classOf[StorageNodeFailedRead], StorageNodeReadResponseSerializer)

    // object
    val serializedData = StorageNodeFailedRead(UUID.randomUUID())

    // serialization
    val bos    = new ByteArrayOutputStream()
    val output = new Output(bos)
    val _      = kryo.writeObject(output, serializedData)
    output.flush()

    // deserialization
    val bis              = new ByteArrayInputStream(bos.toByteArray)
    val input            = new Input(bis)
    val deserializedData = kryo.readObject(input, classOf[StorageNodeFailedRead])

    serializedData shouldBe deserializedData
  }

  it should "serialize/deserialize StorageNodeConflictedRead" in {
    // kryo init
    val kryo = new Kryo()
    kryo.register(classOf[StorageNodeConflictedRead], StorageNodeReadResponseSerializer)

    // object
    val data1 = Data(
      id        = UUID.randomUUID(),
      value     = "some value 1",
      vclock    = VectorClock[NodeId](Map(NodeId(1) -> Counter(3))),
      timestamp = System.currentTimeMillis()
    )
    val data2 = Data(
      id        = UUID.randomUUID(),
      value     = "some value 2",
      vclock    = VectorClock[NodeId](Map(NodeId(1) -> Counter(1))),
      timestamp = System.currentTimeMillis()
    )
    val serializedData = StorageNodeConflictedRead(List(data1, data2))

    // serialization
    val bos    = new ByteArrayOutputStream()
    val output = new Output(bos)
    val _      = kryo.writeObject(output, serializedData)
    output.flush()

    // deserialization
    val bis              = new ByteArrayInputStream(bos.toByteArray)
    val input            = new Input(bis)
    val deserializedData = kryo.readObject(input, classOf[StorageNodeConflictedRead])

    serializedData shouldBe deserializedData
  }
}
