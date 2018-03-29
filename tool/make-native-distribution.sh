#!/bin/bash

# This script creates a distribution of TruffleRuby and Sulong
# compiled to a native image by Substrate VM.

# Do not use jt.rb here to minimze dependencies and avoid compatibility problems
# between the current commit and the built tag's jt.rb.

set -e
set -x

# Tag to use for the different repositories
# If empty, take the current truffleruby revision and imports from suite.py
TAG=""
# TAG=vm-enterprise-32

# In which directory to create the release, will contain the final archive
PREFIX="../release"

# Check platform
case $(uname) in
  Linux)
    os=linux
    ;;
  Darwin)
    os=darwin
    ;;
  *)
    echo "unknown platform $(uname)" 1>&2
    exit 1
    ;;
esac

# Check architecture
case $(uname -m) in
  x86_64)
    arch=amd64
    ;;
  *)
    echo "unknown architecture $(uname -m)" 1>&2
    exit 1
    ;;
esac

original_repo=$(pwd -P)
revision=$(git rev-parse --short HEAD)

rm -rf "${PREFIX:?}"/*
mkdir -p "$PREFIX"

# Expand $PREFIX
PREFIX=$(cd "$PREFIX" && pwd -P)

mkdir -p "$PREFIX/build"
cd "$PREFIX/build"

if [ -n "$TAG" ]; then
  git clone --branch $TAG "$(mx urlrewrite https://github.com/oracle/truffleruby.git)"
  git clone --branch $TAG "$(mx urlrewrite https://github.com/graalvm/sulong.git)"
  git clone --branch $TAG "$(mx urlrewrite https://github.com/oracle/graal.git)"
else
  git clone "$original_repo" truffleruby
fi

cd truffleruby

build_home=$(pwd -P)

release_home="$PREFIX/truffleruby"
mkdir -p "$release_home"

# Use our own GEM_HOME when building
export TRUFFLERUBY_RESILIENT_GEM_HOME=true

# Build
cd ../truffleruby
mx build

cd ../graal/substratevm
mx build

# Build image
mx fetch-languages --Language:llvm --Language:ruby

./native-image --no-server --Language:llvm --Language:ruby \
  -H:Path="$build_home/bin" -H:Name=native-ruby \
  -Dtruffleruby.native.libsulong_dir=lib/cext/sulong-libs \
  -Dtruffleruby.native.resilient_gem_home=true

# Copy TruffleRuby tar distribution
cp "$build_home/mxbuild/$os-$arch/dists/truffleruby-zip.tar" "$release_home"
cd "$release_home"
tar xf truffleruby-zip.tar
rm truffleruby-zip.tar

# Copy the image
cp "$build_home/bin/native-ruby" bin/truffleruby

# Create archive
cd "$PREFIX"
archive_name="truffleruby-native-$TAG-$os-$arch-$revision.tar.gz"
tar czf "$archive_name" truffleruby

# Upload the archive
if [ -n "$UPLOAD_URL" ]; then
  curl -X PUT --netrc -T "$archive_name" "$UPLOAD_URL/$archive_name"
fi

# Test it
"$release_home/bin/ruby" -v

cd "$build_home"
tool/jt.rb test --native -t "$release_home/bin/truffleruby"
