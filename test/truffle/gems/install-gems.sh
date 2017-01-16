#!/usr/bin/env bash

source test/truffle/common.sh.inc

set -e
set -x

bin/jruby-truffle -r bundler-workarounds -S gem install execjs -v 2.6.0
bin/jruby-truffle -r bundler-workarounds -S gem install rack -v 1.6.1
bin/jruby-truffle -r bundler-workarounds -S gem install tilt -v 2.0.1
bin/jruby-truffle -r bundler-workarounds -S gem install rack-protection -v 1.5.3
bin/jruby-truffle -r bundler-workarounds -S gem install sinatra -v 1.4.6
bin/jruby-truffle -r bundler-workarounds -S gem install asciidoctor -v 1.5.4
