#!/bin/bash

# This script creates a standalone native distribution of TruffleRuby and Sulong
# by using the vm suite and the Ruby installable.

# To ensure Bash reads the file at once and allow concurrent editing, run with:
# cat tool/make-standalone-distribution.sh | JAVA_HOME=... bash

set -e
set -x

# Tag to use for the different repositories
# If empty, take the current truffleruby revision and imports from suite.py
TAG=""
# TAG=vm-1.0.0-rc2

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

if [ -z "$JAVA_HOME" ]; then
  echo "JAVA_HOME should be set" 1>&2
  exit 1
fi

original_repo=$(pwd -P)
revision=$(git rev-parse --short=8 HEAD)

if [ -n "$TAG" ]; then
  version="${TAG#vm-}" # Remove the leading "vm-"
else
  version="$revision"
fi

rm -rf "${PREFIX:?}"/*
mkdir -p "$PREFIX"

# Expand $PREFIX
PREFIX=$(cd "$PREFIX" && pwd -P)

mkdir -p "$PREFIX/build"
cd "$PREFIX/build"

if [ -n "$TAG" ]; then
  git clone --depth 1 --branch $TAG "$(mx urlrewrite https://github.com/oracle/truffleruby.git)"
  git clone --depth 1 --branch $TAG "$(mx urlrewrite https://github.com/graalvm/sulong.git)"
  git clone --depth 1 --branch $TAG "$(mx urlrewrite https://github.com/oracle/graal.git)"
else
  git clone "$original_repo" truffleruby

  if [ -d "$original_repo/../graal" ] && [ -d "$original_repo/../sulong" ]; then
    # Building locally (not in CI), copy from local repositories to gain time
    git clone "$original_repo/../graal" graal
    git clone "$original_repo/../sulong" sulong
  fi

  mx -p truffleruby sforceimports
fi

# Use our own GEM_HOME when building
export TRUFFLERUBY_RESILIENT_GEM_HOME=true

# Build
cd truffleruby
build_home=$(pwd -P)

cd ../graal/vm
mx sversions
mx --disable-polyglot --disable-libpolyglot --dy truffleruby,/substratevm build

# The archive basename should be inferable from the version and platform,
# so that Ruby installers know how to find the archive of a given version.
archive_basename="truffleruby-$version-$os-$arch"

release_home="$PREFIX/$archive_basename"
cd "mxbuild/$os-$arch/RUBY_INSTALLABLE_SVM"
cp -R jre/languages/ruby "$release_home"

# Remove unused files in a native-only distribution
rm "$release_home/native-image.properties"
rm "$release_home"/*.jar

# Create archive
cd "$PREFIX"
archive_name="$archive_basename.tar.gz"
tar czf "$archive_name" "$archive_basename"

# Upload the archive
if [ -n "$UPLOAD_URL" ]; then
  curl -X PUT --netrc -T "$archive_name" "$UPLOAD_URL/$archive_name"
fi

# Test it
"$release_home/bin/ruby" -v

cd "$build_home"
AOT_BIN="$release_home/bin/truffleruby" tool/jt.rb test --native --no-home :all
