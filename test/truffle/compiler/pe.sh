#!/usr/bin/env bash

source test/truffle/common.sh.inc

export TRUFFLERUBY_ALLOW_PRIVATE_PRIMITIVES_IN="$truffle/compiler/"
jt ruby --check-compilation --experimental-options --compiler.IterativePartialEscape --engine.MultiTier=false test/truffle/compiler/pe/pe.rb "$@"
