#!/bin/bash

# This script creates a small (~12MB) distribution of TruffleRuby without Sulong
# which only needs JRE 8, curl, wget and bash.

# This script should be run on a fresh checkout of truffleruby,
# otherwise extra gems and build artifacts might be included.
# You should use OpenJDK to build TruffleRuby.

set -e

PREFIX="$1"

if [ -z "$PREFIX" ]; then
  echo "usage: $0 PREFIX"
  exit 1
fi

function copy {
  local dir
  for file in "$@"; do
    dir=$(dirname "$file")
    mkdir -p "$PREFIX/$dir"
    install "$file" "$PREFIX/$dir"
  done
}

revision=$(git rev-parse --short HEAD)

# Build
grep MX_BINARY_SUITES mx.truffleruby/env 2>/dev/null || echo MX_BINARY_SUITES=truffle,sdk >> mx.truffleruby/env
export TRUFFLERUBY_CEXT_ENABLED=false
tool/jt.rb build

rm -rf "${PREFIX:?}"/*
mkdir -p "$PREFIX"

# Copy distributions
copy mxbuild/dists/truffleruby.jar
copy mxbuild/dists/truffleruby-launcher.jar

copy mx.imports/binary/truffle/mxbuild/dists/truffle-api.jar
copy mx.imports/binary/truffle/mxbuild/dists/truffle-nfi.jar
copy mx.imports/binary/truffle/mxbuild/truffle-nfi-native/bin/libtrufflenfi.so
copy mx.imports/binary/truffle/mx.imports/binary/sdk/mxbuild/dists/graal-sdk.jar

copy mxbuild/dists/truffleruby-zip.tar
cd "$PREFIX"
tar xf mxbuild/dists/truffleruby-zip.tar
rm mxbuild/dists/truffleruby-zip.tar

# Script to setup the environment easily
echo '#!/usr/bin/env bash
export TRUFFLERUBY_RESILIENT_GEM_HOME=true
export TRUFFLERUBY_CEXT_ENABLED=false
export TRUFFLERUBYOPT="-Xgraal.warn_unless=false -Xpatching_openssl=true"
file="${BASH_SOURCE[0]}"
if [ -z "$file" ]; then file="$0"; fi
root=$(cd "$(dirname "$file")" && pwd -P)
export PATH="$root/bin:$PATH"
' > setup_env

source setup_env

# Install bundler as we require a specific version and it's convenient
gem install -E bundler -v 1.14.6

tar czf "truffleruby-$revision.tar.gz" -- *
