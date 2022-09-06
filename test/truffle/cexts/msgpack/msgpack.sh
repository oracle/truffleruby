#!/usr/bin/env bash

source test/truffle/common.sh.inc

gem_test_pack=$(jt gem-test-pack)

ruby_home="$(jt ruby -e 'puts Truffle::Boot.ruby_home')"
export PATH="$ruby_home/bin:$PATH"

cd "$gem_test_pack/gem-testing/msgpack-ruby" || exit 1

bundle config --local cache_path ./gem-cache

bundle install --local --no-cache
bundle exec rake compile
bundle exec rake spec
