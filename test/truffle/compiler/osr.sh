#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --graal --jvm.Dgraal.TruffleCompilationExceptionsAreFatal=true test/truffle/compiler/osr/osr.rb
