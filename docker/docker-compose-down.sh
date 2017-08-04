#!/bin/bash
set -ex

docker stop etcd && docker rm etcd
docker stop justindb-1 && docker rm justindb-1
docker stop justindb-2 && docker rm justindb-2
docker stop justindb-3 && docker rm justindb-3
