# JustinDB

<img src="https://github.com/justin-db/JustinDB/blob/master/logo.png" align="right" width="280" />

[![Build Status](https://travis-ci.org/justin-db/JustinDB.svg?branch=master)](https://travis-ci.org/justin-db/JustinDB)
[![codecov](https://codecov.io/gh/justin-db/JustinDB/branch/master/graph/badge.svg)](https://codecov.io/gh/justin-db/JustinDB)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/f5f10352c6e74aa99d0f996cf0a77124)](https://www.codacy.com/app/mateusz-maciaszekhpc/JustinDB)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
![Project Status](https://img.shields.io/badge/status-beta-yellow.svg)
[![Gitter](https://img.shields.io/badge/gitter-join%20chat-brightgreen.svg)](https://gitter.im/justin-db/Lobby)

**Distributed Key-Value Storage built on top of Scala/Akka**

JustinDB KV is an eventually consistent key-value database that favours write availability.
It’s a faithful implementation of Amazon’s Dynamo, with advanced features such as vector clocks for conflict resolution.
JustinDB is also fault-tolerant. Servers can go up or down at any moment with no single point of failure.

## Summary of techniques

| Problem | Technique  | Advantage  |
|---------|------------|------------|
|Partitioning                      |Consistent Hashing                                    |Incremental Scalability|
|Membership and failure detection  |Gossip-based membership protocol and failure detection|Preserves symmetry and avoids having a centralized registry for storing membership and node liveness information|
|High Availability for writes      |Vector clocks with reconciliation during reads        |Version size is decoupled from update rites|
|Recovering from permanent failures|Anti-entropy using Merkle trees                       |Synchronizes divergent replicas in the background|
|Conflicts resolution              |CRDTs                                                 |Automatic conflict resolution during reads

### Why akka
Its a toolkit and runtime for building highly concurrent applications which comes
with ideas that have been around from some time - actor model.
Besides that it has many welcome features around clustering:

1. load balancing
2. location transparency
3. self maintenance
4. fault tolerance

### Authentication, authorization, validation

In case it's not obvious, Justin performs no authentication, authorization, or any validation of input data. Clients must implement those things themselves.

## System Requirements
JustinDB works with Java 8 and newer.

## Bunch of posts that describe JustinDB
1. [JustinDB - Modern REACTIVE NoSQL database](http://speedcom.github.io/dsp2017/2017/03/14/justindb-modern-reactive-nosql-database.html)
2. [JustinDB - Database Model](http://speedcom.github.io/dsp2017/2017/03/17/justindb-database-model.html)
3. [JustinDB - Pluggable persistent and in-memory storage engines](http://speedcom.github.io/dsp2017/2017/03/24/justindb-support-for-pluggable-persistent-and-in-memory-storage-engines.html)
4. [JustinDB - has got more than 700 commits!](http://speedcom.github.io/dsp2017/2017/04/03/justindb-more-than-seven-hundred-commits.html)
5. [JustinDB - serialization that greatly improves performance](http://speedcom.github.io/dsp2017/2017/04/08/justindb-serilization-that-greatly-improves-performance.html)
6. [JustinDB - replication and partitioning](http://speedcom.github.io/dsp2017/2017/04/13/justindb-replication-and-partitioning.html)
7. [JustinDB - why Scala and Akka?](http://speedcom.github.io/dsp2017/2017/04/15/justindb-why-scala-and-akka.html)
8. [JustinDB - data versioning: Vector Clocks](http://speedcom.github.io/dsp2017/2017/04/21/justindb-data-versioning.html)
9. [JustinDB - HTTP API](http://speedcom.github.io/dsp2017/2017/04/30/justindb-http-api.html)
10. [JustinDB - The Ring](http://speedcom.github.io/dsp2017/2017/05/06/justindb-ring.html)
11. [JustinDB - Preference list](http://speedcom.github.io/dsp2017/2017/05/07/justindb-preference-list.html)
12. [JustinDB - solving data entropy: Read Repair](http://speedcom.github.io/dsp2017/2017/05/13/justindb-read-repair.html)
13. [JustinDB - solving data entropy: Active-Anti Entropy](http://speedcom.github.io/dsp2017/2017/05/14/justindb-active-anti-entropy.html)
14. [JustinDB - executable JAR](http://speedcom.github.io/dsp2017/2017/05/20/justindb-executable-jar.html)