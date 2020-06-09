#!/usr/bin/env bash

source test/truffle/common.sh.inc

cd "$truffle/gems/default-bundled-gems" || exit 1

jt ruby gems2gemfile.rb

cat Gemfile
jt ruby -S bundle install --local

echo 'Check if Gemfile is up to date'
git diff --exit-code .
