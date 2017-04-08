package justin.db.replica

import java.util.UUID

import justin.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db._
import justin.db.replica.ReplicaReadAgreement.ReadAgreement
import justin.db.actors.protocol._

import scala.concurrent.{ExecutionContext, Future}

class ReplicaReadCoordinator(
  nodeId: NodeId, ring: Ring, n: N,
  localDataReader: ReplicaLocalReader,
  remoteDataReader: ReplicaRemoteReader
)(implicit ec: ExecutionContext) extends ((StorageNodeReadRequest, ClusterMembers) => Future[StorageNodeReadResponse]) {

  override def apply(cmd: StorageNodeReadRequest, clusterMembers: ClusterMembers): Future[StorageNodeReadResponse] = cmd match {
    case StorageNodeLocalRead(id)    => readLocalData(id)
    case Internal.ReadReplica(r, id) => coordinateReplicated(r, id, clusterMembers)
  }

  private def readLocalData(id: UUID) = localDataReader.apply(id, new IsPrimaryOrReplica(nodeId, ring))

  private def coordinateReplicated(r: R, id: UUID, clusterMembers: ClusterMembers) = {
    val partitionId = UUID2RingPartitionId.apply(id, ring)
    PreferenceList(partitionId, n, ring).fold(onLeft(id), onRight(r, id, clusterMembers))
  }

  private def onLeft(id: UUID)(err: PreferenceList.Error) = Future.successful(StorageNodeFailedRead(id))

  private def onRight(r: R, id: UUID, clusterMembers: ClusterMembers)(preferenceList: PreferenceList) = {
    gatherReads(r, id, clusterMembers, preferenceList).map { reads =>
      val consensus = new ReplicaReadAgreement().reach(r)(reads)
      consensus2ReadingResult(id)(consensus)
    }
  }

  private def gatherReads(r: R, id: UUID, clusterMembers: ClusterMembers, preferenceList: PreferenceList) = {
    ResolveNodeAddresses(nodeId, preferenceList, clusterMembers) match {
      case ResolvedNodeAddresses(true, remotes)  if remotes.size + 1 >= r.r => (readLocalData(id) zip remoteDataReader.apply(remotes, id)).map(converge)
      case ResolvedNodeAddresses(false, remotes) if remotes.size >= r.r     => remoteDataReader.apply(remotes, id)
      case _                                                                => Future.successful(List(StorageNodeFailedRead(id)))
    }
  }

  private def triggerReadRepairIfConsequent: PartialFunction[ReadAgreement, Unit] = {
    case ReadAgreement.Consequent(data) => println(s"Read Repair is fired of for $data") // TODO: finish this part with real logic
  }

  private def consensus2ReadingResult(id: => UUID): ReadAgreement => StorageNodeReadResponse = {
    case ReadAgreement.Consequent(data) => StorageNodeFoundRead(data)
    case ReadAgreement.Found(data)      => StorageNodeFoundRead(data)
    case ReadAgreement.Conflicts(data)  => StorageNodeConflictedRead(data)
    case ReadAgreement.NotEnoughFound   => StorageNodeNotFoundRead(id)
    case ReadAgreement.AllFailed        => StorageNodeFailedRead(id)
    case ReadAgreement.AllNotFound      => StorageNodeNotFoundRead(id)
  }
}
