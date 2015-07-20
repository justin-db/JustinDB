package com.justindb.actors

import akka.actor.{ Actor, Props }
import akka.routing.FromConfig

class NodeActor extends Actor {

  override def receive: Receive = {
    case "send" => println("works")
  }
}


sealed trait NodeActorMsg

