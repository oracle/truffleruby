#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby test/truffle/integration/tcp-server/tcp-server.rb & test_server
