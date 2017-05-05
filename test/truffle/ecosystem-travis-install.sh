#!/usr/bin/env bash

set -e
set -x

unset GEM_HOME GEM_PATH

TRUFFLERUBY_GEM_TEST_PACK_VERSION=1

curl -OL https://www.dropbox.com/s/mpz7xopz29173jy/truffleruby-gem-test-pack-$TRUFFLERUBY_GEM_TEST_PACK_VERSION.tar.gz
tar -zxf truffleruby-gem-test-pack-$TRUFFLERUBY_GEM_TEST_PACK_VERSION.tar.gz
