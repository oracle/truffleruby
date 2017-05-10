#!/usr/bin/env bash

set -e
set -x

test -d ../jruby-truffle-gem-test-pack/gem-testing

truffle_ruby=$(pwd)
JTR="${truffle_ruby}/bin/truffleruby ${truffle_ruby}/lib/truffleruby-tool/bin/truffleruby-tool"
rails_app="${truffle_ruby}/../jruby-truffle-gem-test-pack/gem-testing/rails-app"

cd "${rails_app}"

if [ -n "$CI" -a -z "$HAS_REDIS" ]
then
    echo "No Redis. Skipping rails test."

else

    if [ -f tmp/pids/server.pid ]
    then
        kill "$(cat tmp/pids/server.pid)" || true
        rm tmp/pids/server.pid
    fi

    ${JTR} setup --offline
    ${JTR} run --offline -- -S bundle exec bin/rails server &

    url="http://localhost:3000"

    set +x
    while ! curl -s "$url/people.json";
    do
        echo -n .
        sleep 1
    done
    set -x

    echo Server is up

    curl -s -X "DELETE" "$url/people/destroy_all.json"
    test "$(curl -s "$url/people.json")" = '[]'
    curl -s --data 'name=Anybody&email=ab@example.com' "$url/people.json"
    curl -s "$url/people.json" | grep '"name":"Anybody","email":"ab@example.com"'
    curl -s -X "DELETE" "$url/people/destroy_all.json"

    kill %1
    kill "$(cat tmp/pids/server.pid)"

fi
