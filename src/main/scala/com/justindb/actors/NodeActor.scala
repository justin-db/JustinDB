package com.justindb.actors

import akka.actor.{ Actor, ActorRef, Props }
import akka.routing.FromConfig

class NodeActor extends Actor {

  override def receive: Receive = {
    case "send" => println("works")
  }
}

case class Node(key: Key, a: ActorRef)

sealed trait NodeActorMsg