#!/usr/bin/env bash

source test/truffle/common.sh.inc

gem_test_pack=$(jt gem-test-pack)

jt ruby \
  "-I$gem_test_pack/gems/gems/RubyInline-3.12.4/lib" \
  "-I$gem_test_pack/gems/gems/ZenTest-4.11.1/lib" \
  test/truffle/cexts/RubyInline/ruby_inline_fact.rb
