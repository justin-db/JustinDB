package justin.db.kryo

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import justin.consistent_hashing.NodeId
import justin.db.actors.protocol.RegisterNode
import org.scalatest.{FlatSpec, Matchers}

class RegisterNodeSerializerTest extends FlatSpec with Matchers {

  behavior of "RegisterNode Serializer"

  it should "serialize/deserialize correctly" in {
    // kryo init
    val kryo = new Kryo()
    kryo.register(classOf[RegisterNode], RegisterNodeSerializer)

    // object
    val serializedData = RegisterNode(NodeId(1))

    // serialization
    val bos    = new ByteArrayOutputStream()
    val output = new Output(bos)
    val bytes  = kryo.writeObject(output, serializedData)
    output.flush()

    // deserialization
    val bis              = new ByteArrayInputStream(bos.toByteArray)
    val input            = new Input(bis)
    val deserializedData = kryo.readObject(input, classOf[RegisterNode])

    serializedData shouldBe deserializedData
  }
}
