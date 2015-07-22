package com.justindb

import akka.actor.ActorRef

case class Node(key: Key, underlyingActor: ActorRef)