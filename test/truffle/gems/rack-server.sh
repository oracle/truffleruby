#!/usr/bin/env bash

source test/truffle/common.sh.inc

set -e

jt ruby -I`jt gem-test-pack`/gems/gems/rack-1.6.1/lib test/truffle/gems/rack-server/rack-server.rb & test_server
