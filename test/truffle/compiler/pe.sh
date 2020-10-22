#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --experimental-options --check-compilation --engine.IterativePartialEscape --engine.MultiTier=false test/truffle/compiler/pe/pe.rb "$@"
