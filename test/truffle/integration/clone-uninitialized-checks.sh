#!/usr/bin/env bash

source test/truffle/common.sh.inc

ruby_version=$(jt ruby -v)
if [[ $ruby_version =~ "GraalVM CE JVM" ]] || [[ $ruby_version =~ "GraalVM CE Native" ]]; then
  echo "The checks are only meaningful when splitting is performed so GraalVM compiler is required."
  exit 0
fi

export JT_SPECS_COMPILATION=false
jt test fast -- --check-clone-uninitialized-correctness
