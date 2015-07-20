package com.justindb

import akka.actor.ActorSystem
import com.justindb.actors.ConsistentHashingActor
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props

object ConsistentHashingApp extends App {

  val port = if (args.isEmpty) "0" else args(0)
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.parseString("akka.cluster.roles = [hashing]")).
    withFallback(ConfigFactory.load())

  val system = ActorSystem("ClusterSystem", config)
  val consistentHashingActor = system.actorOf(Props[ConsistentHashingActor], name = "hashing")

}
