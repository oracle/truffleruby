#!/usr/bin/env bash

set -e
set -x

unset GEM_HOME GEM_PATH

TRUFFLERUBY_GEM_TEST_PACK_VERSION=2

jt gem-test-pack
