#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt gem-test-pack

jt ruby -S gem install --local "$(jt gem-test-pack)/gem-cache/json-2.1.0.gem" -V -N --backtrace

output=$(jt --silent ruby -e 'gem "json"; require "json"; puts JSON.dump({ a: 1 })')

if [ "$output" = '{"a":1}' ]; then
  echo Success
else
  echo Unexpected output
  echo "$output"
  exit 1
fi
