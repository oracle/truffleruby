#!/usr/bin/env bash

source test/truffle/common.sh.inc

gem_test_pack=$(jt gem-test-pack)

jt ruby "$gem_test_pack"/gems/gems/asciidoctor-1.5.5/bin/asciidoctor --attribute last-update-label!= test/truffle/gems/asciidoctor/userguide.adoc

if ! cmp --silent test/truffle/gems/asciidoctor/userguide.html test/truffle/gems/asciidoctor/userguide-expected.html
then
  echo Asciidoctor output was not as expected
  diff -u test/truffle/gems/asciidoctor/userguide-expected.html test/truffle/gems/asciidoctor/userguide.html
  rm -f test/truffle/gems/asciidoctor/userguide.html
  exit 1
else
  rm -f test/truffle/gems/asciidoctor/userguide.html
fi
