#!/usr/bin/env bash

source test/truffle/common.sh.inc

platform="$(uname -s)-$(uname -m)"

if [[ "$platform" == "Linux-aarch64" ]]; then
  echo '[GR-41749] Skipping test on linux-aarch64 as it fails'
  exit 0
fi

gem_test_pack=$(jt gem-test-pack)

# The sqlite3 extconf.rb needs pkg-config
command -v pkg-config
pkg-config --version

jt gem install --local "$gem_test_pack/gem-cache/mini_portile2-2.8.0.gem" -V -N --backtrace
jt gem install --local "$gem_test_pack/gem-cache/sqlite3-1.5.3.gem" -V -N --backtrace

jt ruby test/truffle/cexts/sqlite3/test.rb
