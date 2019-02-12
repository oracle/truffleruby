#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --jvm.verbose:gc -e 'p :hi' > temp.txt

if grep 'Full GC' temp.txt
then
  echo "Unexpected Full GC during startup"
  cat temp.txt
  rm temp.txt
  exit 1
fi

if grep 'Metadata' temp.txt
then
  echo "Unexpected Metadata GC during startup"
  cat temp.txt
  rm temp.txt
  exit 1
fi

rm temp.txt
