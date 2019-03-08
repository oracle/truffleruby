#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --graal --vm.Dgraal.TruffleCompilationExceptionsAreThrown=true --vm.Dgraal.TruffleIterativePartialEscape=true --experimental-options --basic_ops.inline=false test/truffle/compiler/pe/pe.rb "$@"
