package justin.db.kryo

import java.util.UUID

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}
import justin.db.actors.protocol.StorageNodeSuccessfulWrite

object StorageNodeSuccessfulWriteSerializer extends Serializer[StorageNodeSuccessfulWrite] {
  override def write(kryo: Kryo, output: Output, successfulWrite: StorageNodeSuccessfulWrite): Unit = {
    output.writeString(successfulWrite.id.toString) // UUID
  }

  override def read(kryo: Kryo, input: Input, `type`: Class[StorageNodeSuccessfulWrite]): StorageNodeSuccessfulWrite = {
    StorageNodeSuccessfulWrite(UUID.fromString(input.readString())) // UUID
  }
}
