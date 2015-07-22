package com.justindb

import akka.actor.ActorSystem
import com.justindb.actors.NodeActor
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props

object NodeApp extends App {

  val port = if (args.isEmpty) "0" else args(0)
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.parseString(s"akka.cluster.roles = [$NodeRole]")).
    withFallback(ConfigFactory.load())

  val system = ActorSystem("ClusterSystem", config)
  val consistentHashingActor = system.actorOf(Props[NodeActor], name = "node")

}