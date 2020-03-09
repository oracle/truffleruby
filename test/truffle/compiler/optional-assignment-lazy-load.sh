#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --engine.CompilationFailureAction=ExitVM test/truffle/compiler/optional-assignment-lazy-load/optional-assignment-lazy-load.rb
