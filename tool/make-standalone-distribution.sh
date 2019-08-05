#!/bin/bash

# This script creates a standalone native-only distribution of TruffleRuby by
# building a small GraalVM with just Ruby and Sulong using the vm suite.

# To ensure Bash reads the file at once and allow concurrent editing, run with:
# cat tool/make-standalone-distribution.sh | JAVA_HOME=... bash

set -e
set -x

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

# Build
tool/jt.rb build --env native

release_home="$(cd ../graal/vm/mxbuild/$os-$arch/RUBY_STANDALONE_SVM*/* && pwd -P)"

# Test the post-install hook
TRUFFLERUBY_RECOMPILE_OPENSSL=true "$release_home/lib/truffle/post_install_hook.sh"

# Test the built distribution

# Use the bin/ruby symlink to test that works too
"$release_home/bin/ruby" -v

# Run all specs
tool/jt.rb -u "$release_home/bin/truffleruby" test :all
