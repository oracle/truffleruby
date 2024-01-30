#!/usr/bin/env bash

source test/truffle/common.sh.inc

export TRUFFLERUBY_CHECK_PREINITIALIZED_SPEC=false

# --always-clone-all covers more than just --core-always-clone, but is much slower (6 min vs 1 min)
JT_SPECS_COMPILATION=false JT_SPECS_SPLITTING=true jt test fast -- --experimental-options --core-always-clone --check-clone-uninitialized-correctness
