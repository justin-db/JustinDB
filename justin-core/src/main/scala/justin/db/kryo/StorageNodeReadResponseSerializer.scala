package justin.db.kryo

import java.util.UUID

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}
import justin.db.Data
import justin.db.actors.protocol._

object StorageNodeReadResponseSerializer extends Serializer[StorageNodeReadResponse] {

  private object Discriminator {
    val Found      = 1
    val Conflicted = 2
    val NotFound   = 3
    val Failed     = 4
  }

  override def write(kryo: Kryo, output: Output, readResponse: StorageNodeReadResponse): Unit = readResponse match {
    case StorageNodeFoundRead(data)           =>
      output.writeInt(Discriminator.Found)
      DataSerializer.write(kryo, output, data)
    case StorageNodeConflictedRead(conflicts) =>
      output.writeInt(Discriminator.Conflicted)
      ListOfDataSerializer.write(kryo, output, conflicts)
    case StorageNodeNotFoundRead(id)          =>
      output.writeInt(Discriminator.NotFound)
      output.writeString(id.toString)
    case StorageNodeFailedRead(id)            =>
      output.writeInt(Discriminator.Failed)
      output.writeString(id.toString)
  }

  override def read(kryo: Kryo, input: Input, `type`: Class[StorageNodeReadResponse]): StorageNodeReadResponse = {
    input.readInt() match {
      case Discriminator.Found      => StorageNodeFoundRead(DataSerializer.read(kryo, input, classOf[Data]))
      case Discriminator.Conflicted => StorageNodeConflictedRead(ListOfDataSerializer.read(kryo, input, classOf[List[Data]]))
      case Discriminator.NotFound   => StorageNodeNotFoundRead(UUID.fromString(input.readString()))
      case Discriminator.Failed     => StorageNodeFailedRead(UUID.fromString(input.readString()))
    }
  }
}
