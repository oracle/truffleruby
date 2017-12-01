#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby -Xplatform.native=false -Xpolyglot.stdio=true -Xsync.stdio=true \
        -Xpatching=false --disable-gems -e 'p 3*4' > temp.txt


if ! cmp --silent temp.txt test/truffle/integration/no_native/expected.txt
then
  echo "No native output was not as expected. Actual:"
  cat temp.txt
  echo "Expected:"
  cat test/truffle/integration/no_native/expected.txt
  rm -f temp.txt
  exit 1
else
  rm -f temp.txt
fi
