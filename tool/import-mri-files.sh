#!/usr/bin/env bash

# This file should pass `shellcheck tool/import-mri-files.sh`.

set -x
set -e

topdir=$(cd ../ruby && pwd -P)

if [ -z "$RUBY_BUILD_DIR" ]; then
  tag=$(cd "$topdir" && git describe --tags)
  version=$(echo "$tag" | tr -d v | tr '_' '.')
  RUBY_BUILD_DIR=$HOME/src/ruby-$version
fi

if [ ! -d "$RUBY_BUILD_DIR" ]; then
  echo "$RUBY_BUILD_DIR does not exist!"
  exit 1
fi

# Generate ext/rbconfig/sizeof/sizes.c and limits.c
(
  cd ../ruby/ext/rbconfig/sizeof
  cp depend Makefile
  make sizes.c limits.c RUBY=ruby top_srcdir="$topdir"
  rm Makefile
)

# lib/
rm -r lib/mri
cp -R ../ruby/lib lib/mri
# Documentation, not code
rm lib/mri/racc/rdoc/grammar.en.rdoc
# We have our own version under lib/truffle
rm lib/mri/timeout.rb
rm lib/mri/weakref.rb
# Uses RubyVM
rm lib/mri/debug.rb
# Files not actually installed in MRI
find lib/mri -name '*.gemspec' -delete
find lib/mri -name '.document' -delete

# *.c
cp ../ruby/st.c src/main/c/cext/st.c

# Copy Ruby files in ext/, sorted alphabetically
cp -R ../ruby/ext/bigdecimal/lib/bigdecimal lib/mri
mkdir lib/mri/digest
cp ../ruby/ext/date/lib/date.rb lib/mri
cp -R ../ruby/ext/digest/sha2/lib/* lib/mri/digest
cp -R ../ruby/ext/fiddle/lib/fiddle lib/mri
cp -R ../ruby/ext/fiddle/lib/fiddle.rb lib/mri
cp ../ruby/ext/nkf/lib/*.rb lib/mri
cp ../ruby/ext/monitor/lib/*.rb lib/mri
cp -R ../ruby/ext/openssl/lib/* lib/mri
cp ../ruby/ext/pty/lib/*.rb lib/mri
cp ../ruby/ext/psych/lib/psych.rb lib/mri
cp -R ../ruby/ext/psych/lib/psych lib/mri
cp ../ruby/ext/ripper/lib/ripper.rb lib/mri
cp -R ../ruby/ext/ripper/lib/ripper lib/mri
cp ../ruby/ext/socket/lib/socket.rb lib/truffle/socket/mri.rb
cp -R ../ruby/ext/syslog/lib/syslog lib/mri

# Copy C extensions in ext/, sorted alphabetically
rm -r src/main/c/{bigdecimal,date,etc,io-console,nkf,openssl,psych,rbconfig-sizeof,ripper,syslog,zlib}
mkdir src/main/c/{bigdecimal,date,etc,io-console,nkf,openssl,psych,rbconfig-sizeof,ripper,syslog,zlib}
cp ../ruby/ext/bigdecimal/*.{c,gemspec,h,rb} src/main/c/bigdecimal
cp ../ruby/ext/date/*.{c,gemspec,h,rb} src/main/c/date
cp ../ruby/ext/etc/*.{c,rb} src/main/c/etc
cp ../ruby/ext/io/console/*.{c,rb} src/main/c/io-console
cp ../ruby/ext/nkf/*.{c,rb} src/main/c/nkf
cp -R ../ruby/ext/nkf/nkf-utf8 src/main/c/nkf
cp ../ruby/ext/openssl/*.{c,h,rb} src/main/c/openssl
cp ../ruby/ext/psych/*.{c,h,rb} src/main/c/psych
cp -R ../ruby/ext/psych/yaml src/main/c/psych
cp ../ruby/ext/rbconfig/sizeof/*.{c,rb} src/main/c/rbconfig-sizeof
cp ../ruby/ext/syslog/*.{c,rb} src/main/c/syslog
cp ../ruby/ext/zlib/*.{c,rb} src/main/c/zlib

# Ripper
cp "$RUBY_BUILD_DIR"/{id.h,symbol.h} lib/cext/include/truffleruby/internal
cp "$RUBY_BUILD_DIR"/{node.c,parse.c,lex.c} src/main/c/ripper
cp "$RUBY_BUILD_DIR"/ext/ripper/*.{c,rb} src/main/c/ripper
cp "$RUBY_BUILD_DIR"/ext/ripper/ripper.y src/main/c/ripper/ripper.y.renamed
cp "$RUBY_BUILD_DIR"/{node.h,parse.h,probes.h,probes.dmyh,regenc.h} src/main/c/ripper

# test/
rm -rf test/mri/tests
cp -R ../ruby/test test/mri/tests
rm -rf test/mri/tests/excludes
cp -R ../ruby/ext/-test- test/mri/tests
mkdir test/mri/tests/cext
mv test/mri/tests/-ext- test/mri/tests/cext-ruby
mv test/mri/tests/-test- test/mri/tests/cext-c
find test/mri/tests/cext-ruby -name '*.rb' -print0 | xargs -0 -n 1 sed -i.backup 's/-test-/c/g'
find test/mri/tests/cext-ruby -name '*.backup' -delete
rm -rf test/mri/excludes
git checkout -- test/mri/excludes

# Copy from tool/lib to test/lib
cp -R ../ruby/tool/lib/* test/mri/tests/lib
rm -f test/mri/tests/lib/leakchecker.rb

# basictest/ and bootstraptest/
rm -rf test/basictest
cp -R ../ruby/basictest test/basictest
rm -rf test/bootstraptest
cp -R ../ruby/bootstraptest test/bootstraptest

# Licences
cp ../ruby/BSDL doc/legal/ruby-bsdl.txt
cp ../ruby/COPYING doc/legal/ruby-licence.txt
cp lib/cext/include/ccan/licenses/BSD-MIT doc/legal/ccan-bsd-mit.txt
cp lib/cext/include/ccan/licenses/CC0 doc/legal/ccan-cc0.txt

# include/
rm -rf lib/cext/include/ruby
git checkout lib/cext/include/ruby/config.h
cp -R ../ruby/include/. lib/cext/include

rm -rf lib/cext/include/ccan
cp -R ../ruby/ccan lib/cext/include

internal_headers=({bignum,bits,compile,compilers,complex,error,fixnum,imemo,numeric,parse,rational,re,static_assert,util}.h)
rm -f "${internal_headers[@]/#/lib/cext/include/internal/}"
cp -R "${internal_headers[@]/#/../ruby/internal/}" lib/cext/include/internal

rm -f lib/cext/include/ruby_assert.h && cp ../ruby/ruby_assert.h lib/cext/include/ruby_assert.h

# defs/
cp ../ruby/defs/known_errors.def tool
cp ../ruby/defs/id.def tool
