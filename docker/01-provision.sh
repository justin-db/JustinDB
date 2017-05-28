#!/usr/bin/env bash
set -ex

docker run \
    --name justindb \
    -p 9000:9000 -p 2552:2552 \
    justindb/justindb:0.1