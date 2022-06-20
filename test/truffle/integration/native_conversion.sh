#!/usr/bin/env bash

source test/truffle/common.sh.inc

export TRUFFLERUBY_MSPEC_CONFIG=spec/nativeconversion.mspec
jt test :capi :truffle_capi -- --cexts-to-native-count
