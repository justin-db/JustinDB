package com.justindb

import akka.actor.ActorSystem
import com.justindb.actors.ConsistentHashingActor
import com.typesafe.config.ConfigFactory

object ConsistentHashingApp extends App {

  val config = ConfigFactory.load()
  val system = ActorSystem("ClusterSystem", config)

  system.actorOf(ConsistentHashingActor.props, name = "producer")
}
