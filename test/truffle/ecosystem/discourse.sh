#!/usr/bin/env bash

SELF_PATH=$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")
while [ -h "$SELF_PATH" ]; do
  DIR=$(dirname "$SELF_PATH")
  SYM=$(readlink "$SELF_PATH")
  SELF_PATH=$(cd "$DIR" && cd "$(dirname "$SYM")" && pwd)/$(basename "$SYM")
done

ecosystem=$(dirname "$SELF_PATH")
truffle=$(dirname "$ecosystem")
source "$truffle/common.sh.inc"

alias truffleruby="jt ruby -S"

gem_test_pack_path="$(jt gem-test-pack)"

cd "$gem_test_pack_path/gem-testing/discourse-2.3.4" || exit 2

truffleruby gem install "$gem_test_pack_path/gem-cache/bundler-1.17.3.gem" --force
truffleruby bundle config --local cache_path "$gem_test_pack_path/gem-cache"
truffleruby bundle install --local --no-cache
