#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --engine.CompilationExceptionsAreThrown --engine.IterativePartialEscape test/truffle/compiler/pe/pe.rb "$@"
