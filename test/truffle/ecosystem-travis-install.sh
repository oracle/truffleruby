#!/usr/bin/env bash

set -e
set -x

source test/truffle/common.sh.inc

unset GEM_HOME GEM_PATH

jt gem-test-pack
