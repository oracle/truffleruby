#!/usr/bin/env bash

source test/truffle/common.sh.inc

gem_test_pack=$(jt gem-test-pack)

jt ruby \
  --no-core-load-path \
  -I"$gem_test_pack"/gems/gems/rack-1.6.1/lib \
  -I"$gem_test_pack"/gems/gems/webrick-1.7.0/lib \
  -I"$gem_test_pack"/gems/gems/rack-protection-1.5.3/lib \
  -I"$gem_test_pack"/gems/gems/sinatra-1.4.6/lib \
  -I"$gem_test_pack"/gems/gems/tilt-2.0.1/lib \
  test/truffle/gems/sinatra-server/sinatra-server.rb & test_server
