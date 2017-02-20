#!/usr/bin/env bash

source test/truffle/common.sh.inc

set -e

GEM_HOME=${GEM_HOME:-lib/ruby/gems/2.3.0}

jt ruby test/truffle/gems/sinatra-server/sinatra-server.rb & test_server
