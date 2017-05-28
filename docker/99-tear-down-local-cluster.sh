#!/bin/bash
set -ex

docker stop etcd && docker rm etcd

for ID in {0..2}; do
    docker-machine stop justindb-$ID && \
    docker-machine rm -y justindb-$ID
done
