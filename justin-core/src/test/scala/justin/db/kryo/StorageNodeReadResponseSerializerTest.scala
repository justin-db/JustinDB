package justin.db.kryo

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.UUID

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import justin.consistent_hashing.NodeId
import justin.db.Data
import justin.db.actors.protocol.StorageNodeFoundRead
import justin.vector_clocks.{Counter, VectorClock}
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
    val bytes  = kryo.writeObject(output, serializedData)
    output.flush()

    // deserialization
    val bis              = new ByteArrayInputStream(bos.toByteArray)
    val input            = new Input(bis)
    val deserializedData = kryo.readObject(input, classOf[StorageNodeFoundRead])

    serializedData shouldBe deserializedData
  }
}
