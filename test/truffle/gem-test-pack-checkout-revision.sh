#!/usr/bin/env bash

set -e
set -x

revision=a46c9157cf7a016e5b488587438e60f9cd0dd791

git -C ../jruby-truffle-gem-test-pack checkout ${revision}
