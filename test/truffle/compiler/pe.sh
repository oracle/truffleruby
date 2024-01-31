#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --check-compilation --experimental-options --compiler.IterativePartialEscape --engine.MultiTier=false test/truffle/compiler/pe/pe.rb "$@"
