#!/usr/bin/env bash

source test/truffle/common.sh.inc

for f in test/truffle/integration/backtraces/*.rb
do
  echo "$f"
  if [ "$(basename "$f")" != "javascript.rb" ]; then
    jt ruby --no-core-load-path "$f"
  fi
done

if [ "$(jt ruby -e 'p TruffleRuby.native?')" = "false" ]; then
  jt ruby --no-core-load-path --polyglot --experimental-options --single_threaded test/truffle/integration/backtraces/javascript.rb
fi
