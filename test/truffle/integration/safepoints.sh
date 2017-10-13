#!/usr/bin/env bash

source test/truffle/common.sh.inc

if out=$(jt ruby test/truffle/integration/safepoints/exit_when_blocked.rb 2>&1); then
  echo "$out"
  echo "The script succeeded when it was expected to fail"
  exit 1
else
  # We do not want stacktraces of non-blocked threads
  echo "$out" | grep -v "SafepointManager.step" >/dev/null
  # Stacktrace of the blocked thread
  echo "$out" | grep "DeadBlockNode.deadBlock"
  echo "$out" | grep "terminating the process"
fi
