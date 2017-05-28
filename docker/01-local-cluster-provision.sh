#!/bin/bash
set -ex

# RUN ETCD ON HOST
docker run \
  --detach \
  --name etcd \
  --publish 2379:2379 \
  quay.io/coreos/etcd:v2.3.7 \
  --listen-client-urls http://0.0.0.0:2379 \
  --advertise-client-urls http://192.168.99.100:2379

# PROVISION 3 MACHINES (VBox)
for ID in {0..2}; do
    docker-machine --native-ssh create --driver virtualbox justindb-$ID
done

# RUN JUSTINDB ON PROVISIONED MACHINES
for ID in {0..2}; do
    HOST_IP=$(docker-machine ip justindb-$ID)
    eval $(docker-machine env justindb-$ID)
    docker run \
    --name justindb \
    -p 9000:9000 -p 2551:2551 -d \
    justindb/justindb:0.1 \
        -Djustin.node-id=$ID \
        -Dakka.remote.netty.tcp.hostname=$HOST_IP \
        -Dakka.remote.netty.tcp.bind-port=2551 \
        -Dakka.remote.netty.tcp.bind-hostname=172.17.0.2 \
        -Dakka.cluster.role.storagenode.min-nr-of-members=3
done
