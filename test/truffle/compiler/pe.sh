#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --engine.CompilationExceptionsAreThrown=true --engine.IterativePartialEscape=true test/truffle/compiler/pe/pe.rb "$@"
