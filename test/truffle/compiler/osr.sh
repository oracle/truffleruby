#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --check-compilation test/truffle/compiler/osr/osr.rb
