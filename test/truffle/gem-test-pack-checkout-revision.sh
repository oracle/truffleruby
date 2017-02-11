#!/usr/bin/env bash

set -e
set -x

revision=09136b220896616f1867ce9e4b09a64f140329fc

git -C ../jruby-truffle-gem-test-pack checkout ${revision}
