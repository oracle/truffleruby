#!/usr/bin/env bash
# Hook run after extraction in the installation directory by a Ruby installer.
# Useful to perform tasks that depend on the user machine and
# cannot be generally done in advance before building the release tarball.

lib_truffle=$(cd "$(dirname "$0")" && pwd -P)
root=$(dirname "$(dirname "$lib_truffle")")

echo "TruffleRuby was sucessfully installed in $root"
