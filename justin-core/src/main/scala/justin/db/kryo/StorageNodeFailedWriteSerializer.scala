package justin.db.kryo

import java.util.UUID

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}
import justin.db.actors.protocol.StorageNodeFailedWrite

object StorageNodeFailedWriteSerializer extends Serializer[StorageNodeFailedWrite] {
  override def write(kryo: Kryo, output: Output, failedWrite: StorageNodeFailedWrite): Unit = {
    output.writeString(failedWrite.id.toString) // UUID
  }

  override def read(kryo: Kryo, input: Input, `type`: Class[StorageNodeFailedWrite]): StorageNodeFailedWrite = {
    StorageNodeFailedWrite(UUID.fromString(input.readString())) // UUID
  }
}
