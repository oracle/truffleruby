#!/usr/bin/env bash

# get the absolute path of the executable and resolve symlinks
SELF_PATH=$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")
while [ -h "$SELF_PATH" ]; do
  # 1) cd to directory of the symlink
  # 2) cd to the directory of where the symlink points
  # 3) get the pwd
  # 4) append the basename
  DIR=$(dirname "$SELF_PATH")
  SYM=$(readlink "$SELF_PATH")
  SELF_PATH=$(cd "$DIR" && cd "$(dirname "$SYM")" && pwd)/$(basename "$SYM")
done

ecosystem=$(dirname "$SELF_PATH")
truffle=$(dirname "$ecosystem")
test=$(dirname "$truffle")
repo=$(dirname "$test")

function jt() {
  ruby "${repo}/tool/jt.rb" "$@"
}

function truffleruby() {
  jt ruby -S "$@"
}

set -xe

gem_test_pack="$(jt gem-test-pack)"

cd test/truffle/ecosystem/blog

truffleruby bundle config --local cache_path "$gem_test_pack/gem-cache"
truffleruby bundle config --local without postgresql mysql

truffleruby bundle install --local --no-cache

truffleruby bin/rails db:setup
truffleruby bin/rails log:clear tmp:clear

truffleruby bin/rails test

if [ -f tmp/pids/server.pid ]; then
  kill "$(cat tmp/pids/server.pid)" || true
  rm tmp/pids/server.pid
fi

port=57085
truffleruby bundle exec bin/rails server --port="$port" &

function kill_server() {
  kill %1
  kill "$(cat tmp/pids/server.pid)"
}

set +x
url="http://localhost:$port/posts.json"
while ! curl -s "$url"; do
  echo -n .
  sleep 1
done
set -x

test "$(curl -s "$url")" = '[]'

kill_server
sleep 5 # wait for the server to finish

# put back the original bin/rake, as it gets overwritten by bundle install
cp $repo/bin/rake $repo/mxbuild/truffleruby-jvm/bin/rake
cp $repo/bin/rake $repo/mxbuild/truffleruby-jvm/jre/bin/rake
cp $repo/bin/rake $repo/mxbuild/truffleruby-jvm/jre/languages/ruby/bin/rake
