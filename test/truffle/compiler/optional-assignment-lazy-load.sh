#!/usr/bin/env bash

source test/truffle/common.sh.inc

export TRUFFLERUBY_ALLOW_PRIVATE_PRIMITIVES_IN="$truffle/compiler/"
jt ruby --check-compilation --experimental-options --engine.MultiTier=false "$PWD/test/truffle/compiler/optional-assignment-lazy-load/optional-assignment-lazy-load.rb"
