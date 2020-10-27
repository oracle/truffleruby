#!/usr/bin/env bash

source test/truffle/common.sh.inc
set +x

if out=$(jt ruby test/truffle/integration/safepoints/exit_when_blocked.rb 2>&1); then
  echo "$out"
  echo "The script succeeded when it was expected to fail"
  exit 1
else
  # Stacktrace of the blocked thread
  if ! [[ "$out" == *"DeadBlockNode.deadBlock"* ]]; then
    echo "No DeadBlockNode.deadBlock"
    echo "$out"
    exit 1
  fi

  if ! [[ "$out" == *"terminating the process"* ]]; then
    echo "No 'terminating the process'"
    echo "$out"
    exit 1
  fi

  echo success
fi
