# JustinDB

[![Build Status](https://travis-ci.org/speedcom/JustinDB.svg?branch=master)](https://travis-ci.org/speedcom/JustinDB)
[![codecov](https://codecov.io/gh/speedcom/JustinDB/branch/master/graph/badge.svg)](https://codecov.io/gh/speedcom/JustinDB)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/f5f10352c6e74aa99d0f996cf0a77124)](https://www.codacy.com/app/mateusz-maciaszekhpc/JustinDB)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

**Distributed Key-Value Storage built on top of Scala/Akka**

JustinDB KV is an eventually consistent key-value database that favours write availability.
It’s a faithful implementation of Amazon’s Dynamo, with advanced features such as vector clocks for conflict resolution.
JustinDB is also fault-tolerant. Servers can go up or down at any moment with no single point of failure.

### Why akka
Its a toolkit and runtime for building highly concurrent applications which comes
with ideas that have been around from some time - actor model.
Besides that it has many welcome features around clustering:

1. load balancing
2. location transparency
3. self maintenance
4. fault tolerance

### System Requirements
JustinDB works with Java 8 and newer.

# Book
This project has dedicated book - http://speedcom.gitbooks.io/justindb/