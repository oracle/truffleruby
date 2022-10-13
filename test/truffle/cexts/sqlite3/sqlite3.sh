#!/usr/bin/env bash

source test/truffle/common.sh.inc

gem_test_pack=$(jt gem-test-pack)

# The sqlite3 extconf.rb needs pkg-config
which pkg-config
pkg-config --version

jt gem install --local "$gem_test_pack/gem-cache/mini_portile2-2.8.0.gem" -V -N --backtrace
jt gem install --local "$gem_test_pack/gem-cache/sqlite3-1.5.3.gem" -V -N --backtrace

jt ruby test/truffle/cexts/sqlite3/test.rb
