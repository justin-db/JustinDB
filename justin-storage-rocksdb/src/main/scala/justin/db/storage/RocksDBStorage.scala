package justin.db.storage

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}
import java.util.UUID

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}
import justin.db.storage.PluggableStorageProtocol.{Ack, StorageGetData}
import org.rocksdb.{FlushOptions, Options, RocksDB}

import scala.concurrent.Future

// TODO:
// Current version store every single data under one file (totally doesn't care about data originality).
// Data should be eventually splitted by ring partitionId.
// This might be an issue during possible data movements between nodes.
final class RocksDBStorage(dir: File) extends PluggableStorageProtocol {
  import RocksDBStorage._

  {
    RocksDB.loadLibrary()
  }

  private[this] val kryo = new Kryo()

  private[this] val db: RocksDB = {
    val options: Options = new Options().setCreateIfMissing(true)
    RocksDB.open(options, dir.getPath)
  }

  override def get(id: UUID)(resolveOriginality: (UUID) => PluggableStorageProtocol.DataOriginality): Future[PluggableStorageProtocol.StorageGetData] = {
    val key: Array[Byte] = uuid2bytes(kryo, id)
    val dataBytes: Array[Byte] = db.get(key)

    val justinDataOpt = Option(dataBytes).map { dataBytes =>
      val input = new Input(new ByteArrayInputStream(dataBytes))
      JustinDataSerializer.read(kryo, input, classOf[JustinData])
    }

    Future.successful(justinDataOpt.map(StorageGetData.Single).getOrElse(StorageGetData.None))
  }

  override def put(data: JustinData)(resolveOriginality: (UUID) => PluggableStorageProtocol.DataOriginality): Future[PluggableStorageProtocol.Ack] = {
    val key: Array[Byte] = uuid2bytes(kryo, data.id)
    val dataBytes: Array[Byte] = {
      val output = new Output(new ByteArrayOutputStream())
      JustinDataSerializer.write(kryo, output, data)
      output.getBuffer
    }

    db.put(key, dataBytes)
    db.flush(new FlushOptions().setWaitForFlush(true))

    Ack.future
  }
}

object RocksDBStorage {

  def uuid2bytes(kryo: Kryo, id: UUID): Array[Byte] = {
    val output = new Output(new ByteArrayOutputStream(), 16)
    UUIDSerializer.write(kryo, output, id)
    output.getBuffer
  }

  object UUIDSerializer extends Serializer[UUID] {
    override def read(kryo: Kryo, input: Input, `type`: Class[UUID]): UUID = {
      new UUID(input.readLong, input.readLong)
    }

    override def write(kryo: Kryo, output: Output, uuid: UUID): Unit = {
      output.writeLong(uuid.getMostSignificantBits)
      output.writeLong(uuid.getLeastSignificantBits)
    }
  }

  object JustinDataSerializer extends Serializer[JustinData] {
    override def read(kryo: Kryo, input: Input, `type`: Class[JustinData]): JustinData = {
      JustinData(
        id        = UUIDSerializer.read(kryo, input, classOf[UUID]),
        value     = input.readString(),
        vclock    = input.readString(),
        timestamp = input.readLong()
      )
    }

    override def write(kryo: Kryo, output: Output, data: JustinData): Unit = {
      UUIDSerializer.write(kryo, output, data.id)
      output.writeString(data.value)
      output.writeString(data.vclock)
      output.writeLong(data.timestamp)
    }
  }
}
