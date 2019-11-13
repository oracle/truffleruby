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

shopt -s expand_aliases
# shellcheck disable=SC2139
alias jt="ruby ${repo}/tool/jt.rb"
alias truffleruby="jt ruby -S"

set -xe

if [ "$2" != "--no-gem-test-pack" ]; then
  gem_test_pack_path="$(jt gem-test-pack)"
fi

cd "test/truffle/ecosystem/$1"

if [ "$gem_test_pack_path" ]; then
  truffleruby bundle config --local cache_path "$gem_test_pack_path/gem-cache"
else
  truffleruby bundle config --delete cache_path
fi

truffleruby bundle config --local without postgresql mysql

if [ "$gem_test_pack_path" ]; then
  truffleruby bundle install --local --no-cache
else
  truffleruby bundle install
fi

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
  wait %1 || true
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

# put back the original bin/rake, as it gets overwritten by bundle install
cp "$repo/bin/rake" "$repo/mxbuild/truffleruby-jvm/bin/rake"
if [ -d "$repo/mxbuild/truffleruby-jvm/jre" ]; then # JDK8
  cp "$repo/bin/rake" "$repo/mxbuild/truffleruby-jvm/jre/bin/rake"
  cp "$repo/bin/rake" "$repo/mxbuild/truffleruby-jvm/jre/languages/ruby/bin/rake"
else # JDK11
  cp "$repo/bin/rake" "$repo/mxbuild/truffleruby-jvm/languages/ruby/bin/rake"
fi
