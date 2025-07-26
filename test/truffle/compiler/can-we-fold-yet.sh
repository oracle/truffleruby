#!/usr/bin/env bash

source test/truffle/common.sh.inc

export TRUFFLERUBY_ALLOW_PRIVATE_PRIMITIVES_IN="$truffle/compiler/"
jt ruby --experimental-options --compiler.IterativePartialEscape --engine.MultiTier=false "$PWD/test/truffle/compiler/can-we-fold-yet/can-we-fold-yet.rb" < test/truffle/compiler/can-we-fold-yet/input.txt > actual.txt

if ! cmp test/truffle/compiler/can-we-fold-yet/expected.txt actual.txt
then
  set +x
  echo Output not as expected
  echo Expected:
  cat test/truffle/compiler/can-we-fold-yet/expected.txt
  if [ -e actual.txt ]
  then
    echo Actual:
    cat actual.txt
    echo Diff:
    diff test/truffle/compiler/can-we-fold-yet/expected.txt actual.txt
    rm -f actual.txt
  fi
  exit 1
fi

rm -f actual.txt
