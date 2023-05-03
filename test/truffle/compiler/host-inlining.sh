#!/usr/bin/env bash

source test/truffle/common.sh.inc

file=${1:-host-inlining.txt}

if [ -f "$file" ]; then
  # shellcheck disable=SC2016
  ruby tool/extract_host_inlining.rb 'org.truffleruby.language.methods.CallForeignMethodNodeGen$ConvertForOperatorAndReDispatchNodeGen.execute' "$file" > out.txt
  # shellcheck disable=SC2016
  grep -F 'Root[org.truffleruby.language.methods.CallForeignMethodNodeGen$ConvertForOperatorAndReDispatchNodeGen.execute]' out.txt
  if ! grep -E 'Out of budget|too big to explore' out.txt; then
    echo 'ConvertForOperatorAndReDispatchNodeGen.execute should be out of budget (too much code), did host inlining output change?'
    cat out.txt
    exit 1
  fi

  ruby tool/extract_host_inlining.rb org.truffleruby.language.dispatch.RubyCallNode.execute "$file" > out.txt
  grep -F 'Root[org.truffleruby.language.dispatch.RubyCallNode.execute]' out.txt
  if grep -E 'Out of budget|too big to explore' out.txt; then
    echo 'RubyCallNode.execute no longer fits in host inlining budget'
    cat out.txt
    exit 1
  fi

  ruby tool/extract_host_inlining.rb --simplify org.truffleruby.language.dispatch.RubyCallNode.execute "$file" | grep CUTOFF > simplified.txt
  if grep -v -E 'not direct call|not inlinable|marked to be not used for inlining|leads to unwind' simplified.txt; then
    echo 'RubyCallNode.execute has unexpected CUTOFFs'
    cat out.txt
    exit 1
  fi

  rm out.txt
  rm simplified.txt
else
  echo "$file does not exist, skipping test"
fi
