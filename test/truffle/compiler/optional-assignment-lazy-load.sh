#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby --check-compilation --engine.MultiTier=false test/truffle/compiler/optional-assignment-lazy-load/optional-assignment-lazy-load.rb
