package justin.db.kryo

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.UUID

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import justin.consistent_hashing.NodeId
import justin.db.Data
import justin.vector_clocks.{Counter, VectorClock}
import org.scalatest.{FlatSpec, Matchers}

class DataSerializerTest extends FlatSpec with Matchers {

  behavior of "Data Serializer"

  it should "serialize/deserialize correctly" in {
    // kryo init
    val kryo = new Kryo()
    kryo.register(classOf[justin.db.Data], DataSerializer)

    // object
    val vClock         = VectorClock[NodeId](Map(NodeId(1) -> Counter(3)))
    val timestamp      = System.currentTimeMillis()
    val serializedData = Data(id = UUID.randomUUID(), value = "some value", vClock, timestamp)

    // serialization
    val bos    = new ByteArrayOutputStream()
    val output = new Output(bos)
    val _      = kryo.writeObject(output, serializedData)
    output.flush()

    // deserialization
    val bis              = new ByteArrayInputStream(bos.toByteArray)
    val input            = new Input(bis)
    val deserializedData = kryo.readObject(input, classOf[Data])

    serializedData shouldBe deserializedData
  }
}
