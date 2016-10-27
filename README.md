# JustinDB

[![Build Status](https://travis-ci.org/speedcom/JustinDB.svg?branch=master)](https://travis-ci.org/speedcom/JustinDB)

**Distributed Key-Value Storage built on top of Scala/Akka**

### Why akka:
Its a toolkit and runtime for builiding highly concurrent applications which comes
with ideas that have been around from some time - actor model.
Besides that it has many welcome features around clustering:
1. load balancing
2. location transparency
3. self maintenance
4. fault tolerance

This project is a result of my own implementation of
different algorithms dedicated for distributed databases such as:
- partitioning (Consistent Hashing)
- replication
- data synchronization (Merkle Trees)
- records versioning (Vector Clocks)
- failured detection (Hinted Handoff)
and so on...

PS: Stay tuned!

PS2: This project has dedicated book - http://speedcom.gitbooks.io/justindb/