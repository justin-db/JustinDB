package justin.db.kryo

import java.util.UUID

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}
import justin.db.Data
import justin.db.versioning.NodeIdVectorClockBase64

object DataSerializer extends Serializer[Data] {
  override def write(kryo: Kryo, output: Output, data: Data): Unit = {
    output.writeString(data.id.toString) // UUID
    output.writeString(data.value)       // Value
    output.writeString(new NodeIdVectorClockBase64().encode(data.vclock).get) // Vector Clock
    output.writeLong(data.timestamp)    // Timestamp
  }

  override def read(kryo: Kryo, input: Input, `type`: Class[Data]): Data = {
    Data(
      id        = UUID.fromString(input.readString()), // UUID
      value     = input.readString(),                  // Value
      vclock    = new NodeIdVectorClockBase64().decode(input.readString()).get, // Vector Clock
      timestamp = input.readLong()                     // Timestamp
    )
  }
}
