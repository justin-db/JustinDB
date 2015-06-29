package com.github.justindb

import com.github.justindb.consistent_hashing.{ ConsistentHashing, Node }

object Main extends App {

  ConsistentHashing.addNodes(realNodes = 5, virtualNodesPerNode = 20)

  println(ConsistentHashing.ring)
  println(ConsistentHashing.get(78))
  println(ConsistentHashing.get("ala"))
  println(ConsistentHashing.get(Node(1, "alalala")))
  println(ConsistentHashing.get(34534534L))

}
