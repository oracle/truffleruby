#!/bin/bash

# This script creates a small (~12MB) distribution of TruffleRuby without Sulong
# which only needs JRE 8, curl and bash.

# This script should be run on a fresh checkout of truffleruby,
# otherwise extra gems and build artifacts might be included.
# You should use OpenJDK to build TruffleRuby.

set -e

PREFIX="$1"
KIND="$2"

if [ -z "$KIND" ]; then
  echo "usage: $0 PREFIX KIND"
  echo "KIND is 'minimal' or 'sulong'"
  exit 1
fi

sulong=false
case "$KIND" in
  minimal)
    ;;
  sulong)
    sulong=true
    ;;
  *)
    echo "KIND must be 'minimal' or 'sulong'"
    exit 1
    ;;
esac

DEST="$PREFIX/truffleruby"

function copy {
  local dir
  for file in "$@"; do
    dir=$(dirname "$file")
    mkdir -p "$DEST/$dir"
    install "$file" "$DEST/$dir"
  done
}

revision=$(git rev-parse --short HEAD)

# Make sure Truffle is used as binary distribution
grep MX_BINARY_SUITES mx.truffleruby/env 2>/dev/null || echo MX_BINARY_SUITES=truffle,sdk >> mx.truffleruby/env

# Setup binary suites
if [ "$sulong" = true ]; then
  if [ -z "${SULONG_REVISION+x}" ]; then
    echo "You need to set SULONG_REVISION (can be '' for latest uploaded)"
    exit 1
  fi
  mx ruby_download_binary_suite sulong "$SULONG_REVISION"
  export TRUFFLERUBY_CEXT_ENABLED=true
  export TRUFFLERUBYOPT="-Xgraal.warn_unless=false"
else
  export TRUFFLERUBY_CEXT_ENABLED=false
  export TRUFFLERUBYOPT="-Xgraal.warn_unless=false -Xpatching_openssl=true"
fi

# Build
tool/jt.rb build

rm -rf "${PREFIX:?}"/*
mkdir -p "$PREFIX"

# Copy distributions
# Truffle
copy mx.imports/binary/truffle/mxbuild/dists/truffle-api.jar
copy mx.imports/binary/truffle/mxbuild/dists/truffle-nfi.jar
copy mx.imports/binary/truffle/mxbuild/truffle-nfi-native/bin/libtrufflenfi.so
copy mx.imports/binary/truffle/mx.imports/binary/sdk/mxbuild/dists/graal-sdk.jar

# Sulong
if [ "$sulong" = true ]; then
  copy mx.imports/binary/sulong/build/sulong.jar
  copy mx.imports/binary/sulong/mxbuild/sulong-libs/libsulong.bc
  copy mx.imports/binary/sulong/mxbuild/sulong-libs/libsulong.so
fi

# TruffleRuby
copy mxbuild/dists/truffleruby.jar
copy mxbuild/dists/truffleruby-launcher.jar

copy mxbuild/dists/truffleruby-zip.tar
cd "$DEST"
tar xf mxbuild/dists/truffleruby-zip.tar
rm mxbuild/dists/truffleruby-zip.tar

# Script to setup the environment easily
cat > setup_env <<EOS
#!/usr/bin/env bash
export TRUFFLERUBY_RESILIENT_GEM_HOME=true
export TRUFFLERUBY_CEXT_ENABLED=$TRUFFLERUBY_CEXT_ENABLED
export TRUFFLERUBYOPT="$TRUFFLERUBYOPT"
EOS
cat >> setup_env <<'EOS'
file="${BASH_SOURCE[0]}"
if [ -z "$file" ]; then file="$0"; fi
root=$(cd "$(dirname "$file")" && pwd -P)
export PATH="$root/bin:$PATH"
EOS

source setup_env

# Install bundler as we require a specific version and it's convenient
gem install -E bundler -v 1.14.6

cd ..
tar czf "truffleruby-$revision.tar.gz" truffleruby
