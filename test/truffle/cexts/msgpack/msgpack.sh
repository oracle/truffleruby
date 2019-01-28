#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt gem-test-pack

ruby_home="$PWD"
export PATH="$ruby_home/bin:$PATH"

gem install "$(jt gem-test-pack)/gem-cache/bundler-1.16.5.gem" --local --no-document

cd "$(jt gem-test-pack)/gem-testing/msgpack-ruby"

bundle config --local cache_path ./gem-cache

bundle install --local --no-cache
bundle exec rake compile
bundle exec rake spec
