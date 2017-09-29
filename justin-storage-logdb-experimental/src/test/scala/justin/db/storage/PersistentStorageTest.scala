package justin.db.storage

import java.io.File
import java.util.UUID

import justin.db.storage.PluggableStorageProtocol.{DataOriginality, StorageGetData}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.util.Random

class PersistentStorageTest extends FlatSpec with Matchers with ScalaFutures {

  behavior of "Persistent Storage"

  it should "instantiate PersistentStorage based on passed journal file representation" in {
    val journalFile = File.createTempFile("journal", ".tmp")
    journalFile.deleteOnExit()

    val persistentStorage = new PersistentStorage(journalFile)

    persistentStorage shouldBe a[PersistentStorage]
  }

  it should "save and get back serialized data from journal file" in {
    val journalFile = File.createTempFile("journal", ".tmp")
    journalFile.deleteOnExit()

    val justinData = List(
      JustinData(id = UUID.randomUUID(), value = "this is exemplary value 1",  vclock = "very-long-vclock1", timestamp = new Random().nextLong()),
      JustinData(id = UUID.randomUUID(), value = "this is exemplary value 2",  vclock = "very-long-vclock2", timestamp = new Random().nextLong()),
      JustinData(id = UUID.randomUUID(), value = "this is exemplary value 3",  vclock = "very-long-vclock3", timestamp = new Random().nextLong())
    )

    {
      val persistentStorage = new PersistentStorage(journalFile)
      justinData.foreach(data => persistentStorage.put(data)(_ => DataOriginality.Primary(1)))
    }

    val persistentStorage = new PersistentStorage(journalFile) // second instance (data is durably saved so we should be able to read them afterward)

    persistentStorage.get(justinData(0).id)(_ => DataOriginality.Primary(1)).futureValue  shouldBe StorageGetData.Single(justinData(0))
    persistentStorage.get(justinData(1).id)(_ => DataOriginality.Primary(1)).futureValue  shouldBe StorageGetData.Single(justinData(1))
    persistentStorage.get(justinData(2).id)(_ => DataOriginality.Primary(1)).futureValue  shouldBe StorageGetData.Single(justinData(2))
    persistentStorage.get(UUID.randomUUID())(_ => DataOriginality.Primary(1)).futureValue shouldBe StorageGetData.None
  }

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(20.seconds, 50.millis)
}
