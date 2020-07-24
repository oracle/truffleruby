#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --check-compilation test/truffle/compiler/optional-assignment-lazy-load/optional-assignment-lazy-load.rb
