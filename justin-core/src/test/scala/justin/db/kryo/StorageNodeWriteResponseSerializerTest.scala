package justin.db.kryo

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.UUID

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import justin.consistent_hashing.NodeId
import justin.db.Data
import justin.db.actors.protocol.{StorageNodeConflictedRead, StorageNodeConflictedWrite, StorageNodeFailedWrite, StorageNodeSuccessfulWrite}
import justin.vector_clocks.{Counter, VectorClock}
import org.scalatest.{FlatSpec, Matchers}

class StorageNodeWriteResponseSerializerTest extends FlatSpec with Matchers {

  behavior of "StorageNodeWriteResponseSerializer Serializer"

  it should "serialize/deserialize StorageNodeSuccessfulWrite" in {
    // kryo init
    val kryo = new Kryo()
    kryo.register(classOf[StorageNodeSuccessfulWrite], StorageNodeWriteResponseSerializer)

    // object
    val serializedData = StorageNodeSuccessfulWrite(UUID.randomUUID())

    // serialization
    val bos    = new ByteArrayOutputStream()
    val output = new Output(bos)
    val bytes  = kryo.writeObject(output, serializedData)
    output.flush()

    // deserialization
    val bis              = new ByteArrayInputStream(bos.toByteArray)
    val input            = new Input(bis)
    val deserializedData = kryo.readObject(input, classOf[StorageNodeSuccessfulWrite])

    serializedData shouldBe deserializedData
  }

  it should "serialize/deserialize StorageNodeFailedWrite" in {
    // kryo init
    val kryo = new Kryo()
    kryo.register(classOf[StorageNodeFailedWrite], StorageNodeWriteResponseSerializer)

    // object
    val serializedData = StorageNodeFailedWrite(UUID.randomUUID())

    // serialization
    val bos    = new ByteArrayOutputStream()
    val output = new Output(bos)
    val bytes  = kryo.writeObject(output, serializedData)
    output.flush()

    // deserialization
    val bis              = new ByteArrayInputStream(bos.toByteArray)
    val input            = new Input(bis)
    val deserializedData = kryo.readObject(input, classOf[StorageNodeFailedWrite])

    serializedData shouldBe deserializedData
  }

  it should "serialize/deserialize StorageNodeConflictedWrite" in {
    // kryo init
    val kryo = new Kryo()
    kryo.register(classOf[StorageNodeConflictedWrite], StorageNodeWriteResponseSerializer)

    // object
    val oldData = Data(
      id        = UUID.randomUUID(),
      value     = "some value 1",
      vclock    = VectorClock[NodeId](Map(NodeId(1) -> Counter(3))),
      timestamp = System.currentTimeMillis()
    )
    val newData = Data(
      id        = UUID.randomUUID(),
      value     = "some value 2",
      vclock    = VectorClock[NodeId](Map(NodeId(1) -> Counter(1))),
      timestamp = System.currentTimeMillis()
    )
    val serializedData = StorageNodeConflictedWrite(oldData, newData)

    // serialization
    val bos    = new ByteArrayOutputStream()
    val output = new Output(bos)
    val bytes  = kryo.writeObject(output, serializedData)
    output.flush()

    // deserialization
    val bis              = new ByteArrayInputStream(bos.toByteArray)
    val input            = new Input(bis)
    val deserializedData = kryo.readObject(input, classOf[StorageNodeConflictedWrite])

    serializedData shouldBe deserializedData
  }
}
