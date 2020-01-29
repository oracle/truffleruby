#!/bin/bash

# This script creates a standalone native-only distribution of TruffleRuby by
# building a small GraalVM with just Ruby and Sulong using the vm suite.

# To ensure Bash reads the file at once and allow concurrent editing, run with:
# cat tool/make-standalone-distribution.sh | JAVA_HOME=... bash

set -e
set -x

# Build
tool/jt.rb build --env native

standalone=$(tool/jt.rb mx --env native standalone-home ruby)
release_home="$PWD/mxbuild/truffleruby-standalone"
rm -rf "$release_home"
cp -R "$standalone" "$release_home"
# Clean build results to make sure nothing refers to them while testing
tool/jt.rb mx --env native clean
rm -rf ../graal/sdk/mxbuild
rm -rf bin lib src

# Test the post-install hook
TRUFFLERUBY_RECOMPILE_OPENSSL=true "$release_home/lib/truffle/post_install_hook.sh"

# Test the built distribution

# Use the bin/ruby symlink to test that works too
"$release_home/bin/ruby" -v

# Run all specs
tool/jt.rb -u "$release_home/bin/truffleruby" test :all
tool/jt.rb -u "$release_home/bin/truffleruby" test :next
