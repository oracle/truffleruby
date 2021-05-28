#!/usr/bin/env bash

source test/truffle/common.sh.inc

code="abort 'not running the GraalVM Compiler' unless TruffleRuby.jit?; puts 'hello'"

# Test both without and with BackgroundCompilation, it catches different issues

jt ruby --engine.UsePreInitializedContext=false --check-compilation --engine.CompileImmediately --engine.BackgroundCompilation=false --trace -e "$code"

jt ruby --engine.UsePreInitializedContext=false --check-compilation --engine.CompileImmediately --trace -e "$code"
