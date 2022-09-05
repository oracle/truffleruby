#!/usr/bin/env bash

source test/truffle/common.sh.inc

gem_test_pack=$(jt gem-test-pack)

ruby_home="$(jt ruby -e 'print RbConfig::CONFIG["prefix"]')"
export PATH="$ruby_home/bin:$PATH"

cd "$truffle/cexts/grpc" || exit 1

bundle config --local cache_path "$gem_test_pack/gem-cache"
bundle install --local --no-cache

output=$(bundle exec ruby -e 'require "grpc"; p GRPC')

if [ "$output" = 'GRPC' ]; then
  echo Success
else
  echo Unexpected output
  echo "$output"
  exit 1
fi
