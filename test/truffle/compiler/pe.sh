#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --experimental-options --check-compilation --compiler.IterativePartialEscape --engine.MultiTier=false test/truffle/compiler/pe/pe.rb "$@"
