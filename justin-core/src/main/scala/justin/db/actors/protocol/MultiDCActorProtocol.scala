package justin.db.actors.protocol

import akka.actor.ActorPath

case class MultiDataCenterContacts(contacts: Set[ActorPath])
case class DataCenterReplica(writeDataReq: StorageNodeWriteRequest)
