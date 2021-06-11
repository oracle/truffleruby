#!/usr/bin/env bash

source test/truffle/common.sh.inc

export TRUFFLERUBY_CHECK_PREINITIALIZED_SPEC=false
jt test fast -- --default-cache=0 --thread-cache=0 --identity-cache=0 --class-cache=0 --array-dup-cache=0 --array-strategy-cache=0 --pack-unroll=0 --global-variable-max-invalidations=0
