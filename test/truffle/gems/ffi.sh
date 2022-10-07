#!/usr/bin/env bash

source test/truffle/common.sh.inc

gem_test_pack=$(jt gem-test-pack)

ruby_home="$(jt ruby -e 'print RbConfig::CONFIG["prefix"]')"
export PATH="$ruby_home/bin:$PATH"

cd spec/ffi || exit 1

# Same gems as msgpack
bundle config --local cache_path "$gem_test_pack/gem-testing/msgpack-ruby/gem-cache"
bundle install --local --no-cache

bundle exec rspec --format doc .
