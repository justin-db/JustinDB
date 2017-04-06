package justin.db.kryo

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.UUID

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import justin.consistent_hashing.NodeId
import justin.db.Data
import justin.db.actors.protocol.StorageNodeWriteDataLocal
import justin.vector_clocks.{Counter, VectorClock}
import org.scalatest.{FlatSpec, Matchers}

class StorageNodeWriteDataLocalSerializerTest extends FlatSpec with Matchers {

  behavior of "StorageNodeWriteDataLocal Serializer"

  it should "serialize/deserialize StorageNodeWriteDataLocal" in {
    // kryo init
    val kryo = new Kryo()
    kryo.register(classOf[StorageNodeWriteDataLocal], StorageNodeWriteDataLocalSerializer)

    // object
    val data = Data(
      id        = UUID.randomUUID(),
      value     = "some value",
      vclock    = VectorClock[NodeId](Map(NodeId(1) -> Counter(3))),
      timestamp = System.currentTimeMillis()
    )
    val serializedData = StorageNodeWriteDataLocal(data)

    // serialization
    val bos    = new ByteArrayOutputStream()
    val output = new Output(bos)
    val bytes  = kryo.writeObject(output, serializedData)
    output.flush()

    // deserialization
    val bis              = new ByteArrayInputStream(bos.toByteArray)
    val input            = new Input(bis)
    val deserializedData = kryo.readObject(input, classOf[StorageNodeWriteDataLocal])

    serializedData shouldBe deserializedData
  }
}
