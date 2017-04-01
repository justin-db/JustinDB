package justin.db.kryo

import java.util.UUID

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}
import justin.db.actors.protocol.StorageNodeLocalRead

object StorageNodeLocalReadSerializer extends Serializer[StorageNodeLocalRead] {

  override def write(kryo: Kryo, output: Output, localRead: StorageNodeLocalRead): Unit = {
    output.writeString(localRead.id.toString)
  }

  override def read(kryo: Kryo, input: Input, `type`: Class[StorageNodeLocalRead]): StorageNodeLocalRead = {
    StorageNodeLocalRead(UUID.fromString(input.readString()))
  }
}
