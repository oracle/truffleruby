#!/usr/bin/env bash

set -x
set -e

repo="../../ffi"

# lib/
rm -rf lib/truffle/ffi
cp -R ../../ffi/lib/ffi lib/truffle

# Keep the empty pointer.rb file, these methods are already defined in core
# and they need to be in core as there are usages in core.
git checkout lib/truffle/ffi/pointer.rb

# spec/
rm -rf spec/ffi
cp -R "$repo/spec/ffi" spec

# Keep the Gemfile files
git checkout spec/ffi/Gemfile spec/ffi/Gemfile.lock


# Remove unused files
rm -rf spec/ffi/embed-test
