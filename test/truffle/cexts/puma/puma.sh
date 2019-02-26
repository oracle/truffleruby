#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt gem-test-pack

jt ruby -S gem install --local "$(jt gem-test-pack)/gem-cache/puma-3.10.0.gem" -V -N --backtrace

jt ruby "-I$(jt gem-test-pack)/gems/gems/rack-1.6.1/lib" -S \
  puma --bind "tcp://127.0.0.1:0" test/truffle/cexts/puma/app.ru & test_server
