#!/usr/bin/env bash

set -e
set -x

test -d ../jruby-truffle-gem-test-pack/gem-testing
bin/truffleruby lib/truffleruby-tool/bin/truffleruby-tool \
    --dir ../jruby-truffle-gem-test-pack/gem-testing ci --offline algebrick
