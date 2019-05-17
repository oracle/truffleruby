#!/usr/bin/env bash

set -e
set -x

# The server should be started on host 127.0.0.1 with port 0 so the OS chooses
# an available port, and this function will find which port was opened.
function test_server {
  serverpid=$!
  echo "Server PID: $serverpid"

  ports=$(jt find_ports_for_pid "$serverpid")

  if [[ $(echo "$ports" | wc -l) -gt 1 ]]; then
    echo "More than one port opened!" >&2
    echo "$ports" >&2
    kill -9 $serverpid
    exit 1
  else
    echo "Found port: $ports"
  fi

  local cmd="curl -s http://localhost:$ports/"
  local response=""
  while ! response=$($cmd) || [[ -z "$response" ]]; do
    sleep 1
  done

  jobs -l
  kill $serverpid
  wait $serverpid || true

  if [[ $response != *"Hello"* ]]
  then
    echo "Response not expected:"
    echo "$response"
    exit 1
  fi
}

shopt -s expand_aliases
alias jt="tool/jt.rb"
