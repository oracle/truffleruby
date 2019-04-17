#!/usr/bin/env bash

set -x
set -e

repo="../../ffi"

rm -rf spec/ffi
cp -R "$repo/spec/ffi" spec

# Remove unused files
rm -rf spec/ffi/embed-test
