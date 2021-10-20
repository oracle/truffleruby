#!/usr/bin/env bash

source test/truffle/common.sh.inc

gem_test_pack=$(jt gem-test-pack)

jt ruby \
  -I"$gem_test_pack"/gems/gems/rack-1.6.1/lib \
  -I"$gem_test_pack"/gems/gems/webrick-1.7.0/lib \
  test/truffle/gems/rack-server/rack-server.rb & test_server
