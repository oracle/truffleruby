#!/usr/bin/env bash

source test/truffle/common.sh.inc

export TRUFFLERUBY_CHECK_PREINITIALIZED_SPEC=false
jt test fast -- --experimental-options --instrument-all-nodes
