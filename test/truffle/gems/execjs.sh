#!/usr/bin/env bash

source test/truffle/common.sh.inc

set -e

jt ruby -I`jt gem-test-pack`/gems/gems/execjs-2.6.0/lib test/truffle/gems/execjs/checkruntime.rb
jt ruby -I`jt gem-test-pack`/gems/gems/execjs-2.6.0/lib test/truffle/gems/execjs/simple.rb
jt ruby -I`jt gem-test-pack`/gems/gems/execjs-2.6.0/lib test/truffle/gems/execjs/coffeescript.rb
