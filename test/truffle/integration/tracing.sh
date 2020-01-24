#!/usr/bin/env bash

source test/truffle/common.sh.inc

for f in test/truffle/integration/tracing/*.rb
do
  echo "$f"
  jt ruby --no-core-load-path "$f"
done
