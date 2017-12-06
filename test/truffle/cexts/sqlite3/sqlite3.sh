#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt gem-test-pack

export TRUFFLERUBY_CEXT_ENABLED=true
jt ruby -S gem install --local "$(jt gem-test-pack)/gem-cache/sqlite3-1.3.13.gem" -V -N --backtrace

jt ruby test/truffle/cexts/sqlite3/test.rb
