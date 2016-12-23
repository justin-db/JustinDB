#!/usr/bin/env bash

set -o errexit
set -o pipefail

__curl_with_retries () {
  local url="$1"
  local payload="$2"
  local success_message="$3"
  local error_message="$4"
  local method="${5:--XPUT}"

  # retry strategy required since curl fails completely on 'connection refused'.
  for i in {1..50}; do
    curl --retry 20 --max-time 20 --fail --silent "$method" "$url" --data "$payload" && \
      _info "$success_message" && break || _warn "$error_message Retry: $i." && sleep "$i"
  done || _error "$error_message. Retries exhausted."
}

_error () {
  local message="$1"
  echo "[ERROR] $message"
}

_warn () {
  local message="$1"
  echo "[WARN] $message"
}

_info () {
  local message="$1"
  echo "[INFO] $message"
}

_consul_url () {
  echo "http://consul1:8500"
}

_internal_ip () {
  echo "$(ip addr show eth0 | awk -F'[/ ]' '/inet / {print $6}' | head -n 1)"
}

_consul_register_service () {
  local name="${1:-$SERVICE_NAME}"
  local port="${2:-${SERVICE_PORT:-80}}"

  local register_url="$(_consul_url)/v1/catalog/register"

  local payload='{
    "Node": "justindb-cluster-'$(_internal_ip)'",
    "Address": "'"$(_internal_ip)"'",
    "Service": {
      "Service": "'"$name"'",
      "Address": "'"$(_internal_ip)"'",
      "Port": '"$port"'
    },
    "Check": {
      "CheckID": "service:'"$name"'",
      "Status": "passing"
    }
  }'

  _info "Attempting to register service: $payload"
  local success_message="Service: '$name' registered!"
  local error_message="Failed to register service: '$id' with local consul agent."

  __curl_with_retries "$register_url" "$payload" "$success_message" "$error_message" &
}

_consul_register_service

export HOST_INTERNAL_IP=$(_internal_ip)

java -jar /app/app.jar