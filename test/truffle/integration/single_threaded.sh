#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby -e 14

jt ruby -Xsingle_threaded -e 14

if jt ruby -Xsingle_threaded -e 'Thread.new { }' 2>/dev/null; then
  echo 'thread creation should have been disallowed' >&2
  exit 1
fi

if jt ruby -Xsingle_threaded -e 'require "timeout"; Timeout.timeout(1) { sleep 2 }' 2>/dev/null; then
  echo 'thread creation should have been disallowed' >&2
  exit 1
fi
