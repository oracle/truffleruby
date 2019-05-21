#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --vm.Dgraal.TruffleCompilationExceptionsAreThrown=true --vm.Dgraal.TruffleIterativePartialEscape=true --experimental-options --basic-ops-inline=false test/truffle/compiler/pe/pe.rb "$@"
