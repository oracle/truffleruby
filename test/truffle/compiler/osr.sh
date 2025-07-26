#!/usr/bin/env bash

source test/truffle/common.sh.inc

export TRUFFLERUBY_ALLOW_PRIVATE_PRIMITIVES_IN="$truffle/compiler/"
jt ruby --check-compilation "$PWD/test/truffle/compiler/osr/osr.rb"
