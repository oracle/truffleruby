#!/usr/bin/env bash

source test/truffle/common.sh.inc

cd "$truffle/gems/default-bundled-gems" || exit 1

jt ruby gems2gemfile.rb

cat Gemfile

# This test only works with no bundle path set
bundle config set --local path.system true

jt ruby -S bundle install --local

echo 'Check if Gemfile is up to date'
git diff --exit-code .
