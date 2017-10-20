#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt gem-test-pack

export TRUFFLERUBY_CEXT_ENABLED=true
jt ruby -S gem install --local "$(jt gem-test-pack)/gem-cache/unf_ext-0.0.7.4.gem" -V -N --backtrace

jt ruby test/truffle/cexts/unf_ext/test.rb
