#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --stress --trace -e "abort 'not running the GraalVM Compiler' unless TruffleRuby.jit?; puts 'hello'"
