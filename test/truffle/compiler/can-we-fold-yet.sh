#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --graal -J-Dgraal.TruffleIterativePartialEscape=true doc/samples/can-we-fold-yet.rb < test/truffle/compiler/can-we-fold-yet/input.txt > actual.txt

if ! cmp test/truffle/compiler/can-we-fold-yet/expected.txt actual.txt
then
  echo Output not as expected
  rm -f actual.txt
  exit 1
fi

rm -f actual.txt
