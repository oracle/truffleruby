#!/usr/bin/env bash
# Hook run after extraction in the installation directory by a Ruby installer.
# Useful to perform tasks that depend on the user machine and
# cannot be generally done in advance before building the release tarball.

set -e

lib_truffle=$(cd "$(dirname "$0")" && pwd -P)
root=$(dirname "$(dirname "$lib_truffle")")

# In case the script shells out to ruby, make sure to use truffleruby and not rely on system ruby
export PATH="$root/bin:$PATH"

cd "$root"

if [ "$TRUFFLERUBY_RECOMPILE_OPENSSL" != "false" ]; then
  # Recompile the OpenSSL C extension to adapt to the system version of OpenSSL
  echo "Compiling the OpenSSL C extension"
  cd src/main/c/openssl
  "$root/bin/truffleruby" -w extconf.rb
  make
  cp openssl.su "$root/lib/mri"
fi

echo "TruffleRuby was sucessfully installed in $root"
