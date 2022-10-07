#!/usr/bin/env bash

source test/truffle/common.sh.inc

gem_test_pack=$(jt gem-test-pack)

jt gem install --local "$gem_test_pack/gem-cache/unf_ext-0.0.7.4.gem" -V -N --backtrace

jt ruby test/truffle/cexts/unf_ext/test.rb
