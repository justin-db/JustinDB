package justin.db.kryo

import java.util.UUID

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}
import justin.db.Data
import justin.db.actors.protocol.{StorageNodeConflictedWrite, StorageNodeFailedWrite, StorageNodeSuccessfulWrite, StorageNodeWriteResponse}

object StorageNodeWriteResponseSerializer extends Serializer[StorageNodeWriteResponse] {

  private object Discriminator {
    val SuccessfulWrite = 1
    val FailedWrite     = 2
    val ConflictedWrite = 3
  }

  override def write(kryo: Kryo, output: Output, response: StorageNodeWriteResponse): Unit = response match {
    case StorageNodeSuccessfulWrite(id)               =>
      output.writeInt(Discriminator.SuccessfulWrite)
      output.writeString(id.toString) // UUID
    case StorageNodeFailedWrite(id)                   =>
      output.writeInt(Discriminator.FailedWrite)
      output.writeString(id.toString) // UUID
    case StorageNodeConflictedWrite(oldData, newData) =>
      output.writeInt(Discriminator.ConflictedWrite)
      DataSerializer.write(kryo, output, oldData)
      DataSerializer.write(kryo, output, newData)
  }

  override def read(kryo: Kryo, input: Input, `type`: Class[StorageNodeWriteResponse]): StorageNodeWriteResponse = input.readInt() match {
    case Discriminator.SuccessfulWrite =>
      StorageNodeSuccessfulWrite(UUID.fromString(input.readString()))
    case Discriminator.FailedWrite     =>
      StorageNodeFailedWrite(UUID.fromString(input.readString()))
    case Discriminator.ConflictedWrite =>
      StorageNodeConflictedWrite(
        oldData = DataSerializer.read(kryo, input, classOf[Data]),
        newData = DataSerializer.read(kryo, input, classOf[Data])
      )
  }
}
