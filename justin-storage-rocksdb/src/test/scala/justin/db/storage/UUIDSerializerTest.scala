package justin.db.storage

import java.io.ByteArrayInputStream
import java.util.UUID

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import justin.db.storage.RocksDBStorage.UUIDSerializer
import org.scalatest.{FlatSpec, Matchers}

class UUIDSerializerTest extends FlatSpec with Matchers {

  behavior of "UUIDSerializer"

  it should "serialize/deserialize UUID with Kryo" in {
    val uuid = UUID.randomUUID()
    val kryo = new Kryo()

    // serialize
    val bytes = RocksDBStorage.uuid2bytes(kryo, uuid)

    // deserialize
    val input = new Input(new ByteArrayInputStream(bytes))
    val id = UUIDSerializer.read(kryo, input, classOf[UUID])

    uuid shouldBe id
  }
}
