#!/usr/bin/env bash

set -e
set -x

source test/truffle/common.sh.inc

jt ruby lib/truffleruby-tool/bin/truffleruby-tool \
    --dir `jt gem-test-pack`/gem-testing ci --offline actionview
