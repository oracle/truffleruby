#!/bin/bash

# This script creates a standalone native-only distribution of TruffleRuby by
# building a small GraalVM with just Ruby and Sulong using the vm suite.
# It renames the resulting archive and directory to create a unique name based
# on the current commit SHA-1 to allow uploading to a common directory.

# To ensure Bash reads the file at once and allow concurrent editing, run with:
# cat tool/make-standalone-distribution.sh | JAVA_HOME=... bash

set -e
set -x

# In which directory to create the release, will contain the final archive
PREFIX="../release"

# Check platform
case $(uname) in
  Linux) os=linux ;;
  Darwin) os=darwin ;;
  *) echo "unknown platform $(uname)" 1>&2; exit 1 ;;
esac

# Check architecture
case $(uname -m) in
  x86_64) arch=amd64 ;;
  *) echo "unknown architecture $(uname -m)" 1>&2; exit 1 ;;
esac

if [ -z "$JAVA_HOME" ]; then
  echo "JAVA_HOME should be set" 1>&2
  exit 1
fi

original_repo=$(pwd -P)
revision=$(git rev-parse --short=8 HEAD)
version="$revision"

if [[ "$CLEAN" != "no" ]]; then
  rm -rf "${PREFIX:?}"/*
  mkdir -p "$PREFIX"
fi

# Expand $PREFIX
PREFIX=$(cd "$PREFIX" && pwd -P)

mkdir -p "$PREFIX/build"
cd "$PREFIX/build"

# Always make a copy of the repository, because we do not want any extra files
# (e.g., extra gems) that might be in the current truffleruby repository.
test -d truffleruby || git clone "$original_repo" truffleruby

# Shortcut when running the script locally
if [ -d "$original_repo/../graal" ]; then
  # Building locally (not in CI), copy from local repositories to gain time
  test -d graal || git clone "$original_repo/../graal" graal
fi

mx -p truffleruby sforceimports

# Use our own GEM_HOME when building
export TRUFFLERUBY_RESILIENT_GEM_HOME=true

# Build
cd truffleruby
repo=$(pwd -P)

cd ../graal/vm
mx --dy truffleruby,/substratevm sversions
mx --disable-polyglot --disable-libpolyglot --force-bash-launchers=lli,native-image --dy truffleruby,/substratevm build

# The archive basename should be inferable from the version and platform,
# so that Ruby installers know how to find the archive of a given version.
archive_basename="truffleruby-$version-$os-$arch"

release_home="$PREFIX/$archive_basename"
# Rename the Ruby standalone distribution created by the vm suite
cp -R mxbuild/$os-$arch/RUBY_STANDALONE_SVM/* "$release_home"

# Create archive
cd "$PREFIX"
archive_name="$archive_basename.tar.gz"
tar czf "$archive_name" "$archive_basename"

# Upload the archive
if [ -n "$UPLOAD_URL" ]; then
  curl -X PUT --netrc -T "$archive_name" "$UPLOAD_URL/$archive_name"
fi

# Test the post-install hook
TRUFFLERUBY_RECOMPILE_OPENSSL=true "$release_home/lib/truffle/post_install_hook.sh"

# Test the built distribution

# Use the bin/ruby symlink to test that works too
"$release_home/bin/ruby" -v

# Run all specs
cd "$repo"
AOT_BIN="$release_home/bin/truffleruby" tool/jt.rb test --native :all
