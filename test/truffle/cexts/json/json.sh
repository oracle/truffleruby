#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt gem-test-pack

jt ruby -S gem install --local "$(jt gem-test-pack)/gem-cache/json-2.1.0.gem" -V -N --backtrace

# Disable debug output by Bash as it creates extra output
set +x

output=$(jt ruby --no-print-cmd -rjson -e 'print JSON' 2>&1)

if [ "$output" = "JSON" ]; then
  echo Success
else
  echo Unexpected output
  echo "$output"
  exit 1
fi
