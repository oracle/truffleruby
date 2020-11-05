#!/usr/bin/env bash

source test/truffle/common.sh.inc

gem_test_pack=$(jt gem-test-pack)

ruby_home="$(jt ruby -e 'print RbConfig::CONFIG["prefix"]')"
export PATH="$ruby_home/bin:$PATH"

cd spec/ffi || exit 1

# Use ruby -S to avoid the nested shebang problem on macOS when using GraalVM Bash launchers
# Same gems as msgpack
ruby -S bundle config --local cache_path "$gem_test_pack/gem-testing/msgpack-ruby/gem-cache"
ruby -S bundle install --local --no-cache

ruby -S bundle exec rspec --format doc .
