#!/usr/bin/env bash

source test/truffle/common.sh.inc

# Simple programs should just work with -Xsingle_threaded

jt ruby -Xsingle_threaded -e 14

# Creating threads should obviously not work

if jt ruby -Xsingle_threaded -e 'Thread.new { }' 2>/dev/null; then
  echo 'thread creation should have been disallowed' >&2
  exit 1
fi

# Using timeout should not raise any exception, but it won't actually work

jt ruby -Xsingle_threaded -e 'require "timeout"; Timeout.timeout(1) { sleep 2 }'

# But it should still yield

jt ruby -Xsingle_threaded -e 'require "timeout"; Timeout.timeout(1) { exit! 0 }; exit! 1'

# Creating objects that use finalization should work

jt ruby -Xsingle_threaded -e 'Rubinius::FFI::MemoryPointer.new(1024)'

# Finalizations should actually be run

jt ruby -Xsingle_threaded test/truffle/integration/single_threaded/finalization.rb
