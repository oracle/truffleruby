#!/usr/bin/env bash

source test/truffle/common.sh.inc

set -e

GEM_HOME=`jt gem-test-pack`/gems/gems

jt ruby test/truffle/gems/sinatra-server/sinatra-server.rb & test_server
