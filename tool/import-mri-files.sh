#!/usr/bin/env bash

set -x
set -e

topdir=$(cd ../ruby && pwd -P)

# Generate ext/rbconfig/sizeof/sizes.c
(
  cd ../ruby/ext/rbconfig/sizeof
  cp depend Makefile
  make sizes.c RUBY=ruby top_srcdir="$topdir"
  rm Makefile
)

# lib/
rm -r lib/mri
cp -r ../ruby/lib lib/mri
rm lib/mri/racc/rdoc/grammar.en.rdoc
rm lib/mri/securerandom.rb
rm lib/mri/timeout.rb
rm lib/mri/weakref.rb
find lib/mri | grep '/.gemspec$' | xargs rm
find lib/mri | grep '/.document$' | xargs rm

# *.c
cp ../ruby/st.c src/main/c/cext/st.c

# ext/, sorted alphabetically
cp -r ../ruby/ext/bigdecimal/lib/bigdecimal lib/mri

cp ../ruby/ext/etc/*.{c,rb} src/main/c/etc

cp ../ruby/ext/nkf/lib/*.rb lib/mri
cp ../ruby/ext/nkf/*.{c,rb} src/main/c/nkf
cp -r ../ruby/ext/nkf/nkf-utf8 src/main/c/nkf

rm src/main/c/openssl/*.{c,h}
cp ../ruby/ext/openssl/*.{c,h,rb} src/main/c/openssl
cp -r ../ruby/ext/openssl/lib/* lib/mri

cp ../ruby/ext/psych/*.{c,h,rb} src/main/c/psych
cp ../ruby/ext/psych/yaml/*.{c,h} src/main/c/psych/yaml
cp ../ruby/ext/psych/yaml/LICENSE src/main/c/psych/yaml
cp ../ruby/ext/psych/lib/psych.rb lib/mri
cp -r ../ruby/ext/psych/lib/psych lib/mri

cp ../ruby/ext/pty/lib/*.rb lib/mri

cp ../ruby/ext/rbconfig/sizeof/*.{c,rb} src/main/c/rbconfig-sizeof

cp ../ruby/ext/syslog/*.{c,rb} src/main/c/syslog
cp -r ../ruby/ext/syslog/lib/syslog lib/mri/syslog

cp ../ruby/ext/zlib/*.{c,rb} src/main/c/zlib

# test/
rm -rf test/mri/tests
cp -r ../ruby/test test/mri/tests
rm -rf test/mri/tests/excludes
cp -r ../ruby/ext/-test- test/mri/tests
mkdir test/mri/tests/cext
mv test/mri/tests/-ext- test/mri/tests/cext-ruby
mv test/mri/tests/-test- test/mri/tests/cext-c
find test/mri/tests/cext-ruby -name '*.rb' -print0 | xargs -0 -n 1 sed -i.backup 's/-test-/c/g'
find test/mri/tests/cext-ruby -name '*.backup' -delete
rm -rf test/mri/excludes
git checkout -- test/mri/excludes
git checkout -- test/mri/tests/runner.rb

# Licences
cp ../ruby/BSDL doc/legal/ruby-bsdl.txt
cp ../ruby/COPYING doc/legal/ruby-licence.txt
cp lib/cext/include/ccan/licenses/BSD-MIT doc/legal/ccan-bsd-mit.txt
cp lib/cext/include/ccan/licenses/CC0 doc/legal/ccan-cc0.txt

# include/
rm -rf lib/cext/include/ruby lib/cext/include/ccan
git checkout lib/cext/include/ruby/config.h
cp -r ../ruby/include/. lib/cext/include
cp -r ../ruby/ccan/. lib/cext/include/ccan
