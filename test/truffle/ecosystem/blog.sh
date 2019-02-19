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
home=$(dirname "$test")

function jt {
  ruby "${home}/tool/jt.rb" "$@"
}

function truffleruby {
  jt ruby -S "$@"
}

set -xe

jt gem-test-pack
truffleruby gem install truffleruby-gem-test-pack/gem-cache/bundler-1.16.5.gem --local --no-document

# backup bin/rake, which gets overwritten
cp bin/rake bin/rake-original

cd test/truffle/ecosystem/blog

truffleruby bundle config --local cache_path ../../../../truffleruby-gem-test-pack/gem-cache
truffleruby bundle config --local without postgresql mysql

truffleruby bundle install --local --no-cache

truffleruby bin/rails db:setup
truffleruby bin/rails log:clear tmp:clear

truffleruby bin/rails test

if [ -f tmp/pids/server.pid ]
then
    kill "$(cat tmp/pids/server.pid)" || true
    rm tmp/pids/server.pid
fi

port=57085
truffleruby bundle exec bin/rails server --port="$port" &

function kill_server {
  kill %1
  kill "$(cat tmp/pids/server.pid)"
}

set +x
url="http://localhost:$port/posts.json"
while ! curl -s "$url";
do
    echo -n .
    sleep 1
done
set -x

test "$(curl -s "$url")" = '[]'

kill_server

# put back the original
cd "$home"
mv -f bin/rake-original bin/rake
