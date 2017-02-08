#!/usr/bin/env bash

set -x
set -e

rm -rf lib/mri
cp -r ../ruby/lib lib/mri
rm lib/mri/racc/rdoc/grammar.en.rdoc
rm lib/mri/timeout.rb
rm lib/mri/weakref.rb
rm -rf lib/mri/webrick*

rm -rf lib/ext/*
cp -r ../ruby/ext/bigdecimal/lib/bigdecimal lib/ext
cp -r ../ruby/ext/psych/lib/psych lib/ext
cp -r ../ruby/ext/psych/lib/*.rb lib/ext
cp -r ../ruby/ext/pty/lib/*.rb lib/ext

rm -rf test/mri
cp -r ../ruby/test test/mri
git checkout -- test/mri/excludes_truffle
rm -rf test/mri/excludes
git checkout -- test/mri/runner.rb

cp ../ruby/BSDL doc/legal/ruby-bsdl.txt
cp ../ruby/COPYING doc/legal/ruby-licence.txt
