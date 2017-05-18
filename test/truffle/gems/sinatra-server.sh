#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby \
  -I`jt gem-test-pack`/gems/gems/rack-1.6.1/lib \
  -I`jt gem-test-pack`/gems/gems/rack-protection-1.5.3/lib \
  -I`jt gem-test-pack`/gems/gems/sinatra-1.4.6/lib \
  -I`jt gem-test-pack`/gems/gems/tilt-2.0.1/lib \
  test/truffle/gems/sinatra-server/sinatra-server.rb & test_server
