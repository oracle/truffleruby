#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby -e 14

jt ruby -Xsingle_threaded -e 14

if jt ruby -Xsingle_threaded -e 'Thread.new { }' 2>/dev/null; then
  echo 'thread creation should have been disallowed' >&2
  exit 1
fi

jt ruby -Xsingle_threaded -e 'require "timeout"; Timeout.timeout(1) { sleep 2 }'

jt ruby -Xsingle_threaded -e 'Rubinius::FFI::MemoryPointer.new(1024)'

jt ruby -Xsingle_threaded -e 'x = Object.new; ObjectSpace.define_finalizer x, -> { exit! 0 }; x = nil; GC.start; sleep 1; y = Object.new; ObjectSpace.define_finalizer y, -> { exit! 1 }; exit! 1'
