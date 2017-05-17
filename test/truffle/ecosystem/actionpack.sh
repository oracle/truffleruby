#!/usr/bin/env bash

set -e
set -x

jt ruby lib/truffleruby-tool/bin/truffleruby-tool \
    --dir `jt gem-test-pack`/gem-testing ci --offline actionpack
