#!/usr/bin/env bash

source test/truffle/common.sh.inc

output=$(jt ruby -Ctest/truffle/gems/bundler-version/two bundler-version.rb)

if [ "$output" = 'true' ]; then
  echo Success
else
  echo Unexpected bundler 2 output
  echo "$output"
  exit 1
fi
