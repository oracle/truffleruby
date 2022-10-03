#!/usr/bin/env bash

source test/truffle/common.sh.inc

export JT_SPECS_COMPILATION=false
jt test fast -- --check-clone-uninitialized-correctness
