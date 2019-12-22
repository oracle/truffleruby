#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --engine.CompilationExceptionsAreFatal=true test/truffle/compiler/stf-optimises/stf-optimises.rb
