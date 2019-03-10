#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt gem-test-pack

ruby_home="$(jt ruby -e 'puts Truffle::Boot.ruby_home')"
export PATH="$ruby_home/bin:$PATH"

cd "$(jt gem-test-pack)/gem-testing/msgpack-ruby"

# Use ruby -S to avoid the nested shebang problem on macOS
ruby -S bundle config --local cache_path ./gem-cache

ruby -S bundle install --local --no-cache
ruby -S bundle exec rake compile
ruby -S bundle exec rake spec
