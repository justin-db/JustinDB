package justin.db.storage

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.UUID

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import justin.db.storage.RocksDBStorage.JustinDataSerializer
import org.scalatest.{FlatSpec, Matchers}

class JustinDataSerializerTest extends FlatSpec with Matchers {

  behavior of "JustinDataSerializer"

  it should "serialize/deserialize JustinData with Kryo" in {
    val kryo = new Kryo()
    val data = JustinData(
      id        = UUID.randomUUID,
      value     = "to jest przykladowa wartość",
      vclock    = "vclock-value",
      timestamp = 1234124L
    )

    // serialize
    val output = new Output(new ByteArrayOutputStream())
    JustinDataSerializer.write(kryo, output, data)
    val dataBytes = output.getBuffer

    // deserialize
    val input = new Input(new ByteArrayInputStream(dataBytes))
    JustinDataSerializer.read(kryo, input, classOf[JustinData]) shouldBe data
  }
}
