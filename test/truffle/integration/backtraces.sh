#!/usr/bin/env bash

source test/truffle/common.sh.inc

for f in test/truffle/integration/backtraces/*.rb
do
  echo "$f"
  options=""
  if [ "$(basename "$f")" = "javascript.rb" ]; then
    options="--polyglot --experimental-options --single_threaded"
  fi
  jt ruby --no-core-load-path $options "$f"
done
