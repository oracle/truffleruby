#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt gem-test-pack

jt ruby -S gem install --local "$(jt gem-test-pack)/gem-cache/msgpack-1.2.4.gem" -V -N --backtrace
