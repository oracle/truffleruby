#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --stress --trace --engine.UsePreInitializedContext=false -e "abort 'not running the GraalVM Compiler' unless TruffleRuby.jit?; puts 'hello'"
