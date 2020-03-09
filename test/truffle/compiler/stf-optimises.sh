#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --experimental-options --engine.CompilationFailureAction=ExitVM test/truffle/compiler/stf-optimises/stf-optimises.rb
