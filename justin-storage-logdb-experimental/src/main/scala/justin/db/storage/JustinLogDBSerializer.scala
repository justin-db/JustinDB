package justin.db.storage

import java.io.{ByteArrayInputStream, DataInputStream}
import java.util.UUID

import logdb.serializer.LogDBSerializer

final class JustinLogDBSerializer extends LogDBSerializer[UUID, JustinData] {

  override protected def writeKey(key: UUID): Array[Byte] = uuid2bytes(key)
  override protected def readKey(serialized: Array[Byte]): UUID = bytes2uuid(serialized)

  override protected def writeValue(data: JustinData): Array[Byte] = {
    // id
    val idBytes = uuid2bytes(data.id)

    // value
    val valueSizeBytes = int2bytes(data.value.length)
    val valueBytes = data.value.getBytes()

    // vclock
    val vclockSizeBytes = int2bytes(data.vclock.length)
    val vclockBytes = data.vclock.getBytes

    // timestamp
    val timestampBytes = long2bytes(data.timestamp)

    idBytes ++ valueSizeBytes ++ valueBytes ++ vclockSizeBytes ++ vclockBytes ++ timestampBytes
  }

  override protected def readValue(serialized: Array[Byte]): JustinData = {
    val bais = new ByteArrayInputStream(serialized)
    val dis = new DataInputStream(bais)

    // id
    val idBuffer = new Array[Byte](16) // UUID
    dis.read(idBuffer)
    val id: UUID = bytes2uuid(idBuffer)

    // value
    val valueSizeBuffer = new Array[Byte](4) // Int
    dis.read(valueSizeBuffer)
    val valueSize: Int = bytes2int(valueSizeBuffer)

    val valueBuffer = new Array[Byte](valueSize)
    dis.read(valueBuffer)
    val value: String = new String(valueBuffer)

    // vclock
    val vclockSizeBuffer = new Array[Byte](4) // Int
    dis.read(vclockSizeBuffer)
    val vclockSize: Int = bytes2int(vclockSizeBuffer)

    val vclockBuffer = new Array[Byte](vclockSize)
    dis.read(vclockBuffer)
    val vclock: String = new String(vclockBuffer)

    // timestamp
    val timestampBuffer = new Array[Byte](8) // Long
    dis.read(timestampBuffer)
    val timestamp: Long = bytes2long(timestampBuffer)

    JustinData(
      id        = id,
      value     = value,
      vclock    = vclock,
      timestamp = timestamp
    )
  }
}
