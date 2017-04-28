#!/usr/bin/env bash

set -e
set -x

unset GEM_HOME GEM_PATH

git clone \
    --branch master \
    https://github.com/jruby/all-ruby-benchmarks.git \
    ../jruby-truffle-gem-test-pack

test/truffle/gem-test-pack-checkout-revision.sh
