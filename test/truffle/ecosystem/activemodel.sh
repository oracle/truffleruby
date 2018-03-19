#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby test/truffleruby-tool/bin/truffleruby-tool \
    --dir $(jt gem-test-pack)/gem-testing ci --offline activemodel
