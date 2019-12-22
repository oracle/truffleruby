#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --engine.CompilationExceptionsAreFatal test/truffle/compiler/osr/osr.rb
