#!/bin/bash

# This script creates a distribution of TruffleRuby
# with/without Sulong and with/without Graal.
# The minimal distribution only needs JRE 8, curl and bash.

# This script should be run on a fresh checkout of truffleruby,
# otherwise extra gems and build artifacts might be included.
# You should use OpenJDK to build TruffleRuby.

set -e

PREFIX="$1"
KIND="$2"

if [ -z "$KIND" ]; then
  echo "usage: $0 PREFIX KIND"
  echo "KIND is 'minimal' or 'sulong' or 'graal' or 'full'"
  exit 1
fi

sulong=false
graal=false

case "$KIND" in
  minimal)
    ;;
  sulong)
    sulong=true
    ;;
  graal)
    graal=true
    ;;
  full)
    sulong=true
    graal=true
    ;;
  *)
    echo "KIND is 'minimal' or 'sulong' or 'graal' or 'full'"
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
  export TRUFFLERUBYOPT="-Xgraal.warn_unless=$graal"
else
  export TRUFFLERUBY_CEXT_ENABLED=false
  export TRUFFLERUBYOPT="-Xgraal.warn_unless=$graal -Xpatching_openssl=true"
fi

if [ "$graal" = true ]; then
  mx ruby_download_binary_suite compiler truffle

  ruby tool/jt.rb install jvmci
  jvmci=$(ruby tool/jt.rb install jvmci 2>/dev/null)
  jvmci_basename=$(basename "$jvmci")
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

# Graal
if [ "$graal" = true ]; then
  graal_dist=mx.imports/binary/compiler/mxbuild/dists/graal.jar
  copy "$graal_dist"

  cp -r "$jvmci" "$DEST"
  # Remove JavaDoc as it's >250MB
  rm -r "$DEST/$jvmci_basename/docs/api"
  rm -r "$DEST/$jvmci_basename/docs/jdk"
  rm -r "$DEST/$jvmci_basename/docs/jre"
  # Removes sources (~50MB)
  rm "$DEST/$jvmci_basename/src.zip"
fi

# TruffleRuby
copy mxbuild/dists/truffleruby.jar
copy mxbuild/dists/truffleruby-launcher.jar

copy mxbuild/linux-amd64/dists/truffleruby-zip.tar
cd "$DEST"
tar xf mxbuild/linux-amd64/dists/truffleruby-zip.tar
rm mxbuild/linux-amd64/dists/truffleruby-zip.tar

# Script to setup the environment easily
cat > setup_env <<'EOS'
#!/usr/bin/env bash
file="${BASH_SOURCE[0]}"
if [ -z "$file" ]; then file="$0"; fi
root=$(cd "$(dirname "$file")" && pwd -P)
export PATH="$root/bin:$PATH"
EOS
cat >> setup_env <<EOS
export TRUFFLERUBY_RESILIENT_GEM_HOME=true
export TRUFFLERUBY_CEXT_ENABLED=$TRUFFLERUBY_CEXT_ENABLED
export TRUFFLERUBYOPT="$TRUFFLERUBYOPT"
EOS
if [ "$graal" = true ]; then
  cat >> setup_env <<EOS
export JAVACMD="\$root/$jvmci_basename/bin/java"
export JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -Djvmci.class.path.append=\$root/$graal_dist"
EOS
fi

source setup_env

# Install bundler as we require a specific version and it's convenient
gem install -E bundler -v 1.14.6

cd "$PREFIX"
tar czf "truffleruby-$revision.tar.gz" truffleruby
