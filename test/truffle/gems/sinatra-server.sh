#!/usr/bin/env bash

source test/truffle/common.sh.inc

gem_test_pack=$(jt gem-test-pack)

jt ruby \
  --no-core-load-path \
  -I"$gem_test_pack"/gems/gems/mustermann-3.0.0/lib \
  -I"$gem_test_pack"/gems/gems/rack-3.0.9/lib \
  -I"$gem_test_pack"/gems/gems/webrick-1.8.1/lib \
  -I"$gem_test_pack"/gems/gems/rack-protection-4.0.0/lib \
  -I"$gem_test_pack"/gems/gems/rack-session-2.0.0/lib \
  -I"$gem_test_pack"/gems/gems/sinatra-4.0.0/lib \
  -I"$gem_test_pack"/gems/gems/rackup-2.1.0/lib \
  -I"$gem_test_pack"/gems/gems/tilt-2.0.1/lib \
  test/truffle/gems/sinatra-server/sinatra-server.rb & test_server
