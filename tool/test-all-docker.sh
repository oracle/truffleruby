#!/bin/bash

set -e

echo "You may want to wipe your Docker caches before running these"

for c in oraclelinux ubuntu fedora rbenv chruby rvm; do
  pushd test/truffle/docker/$c
  docker build -t truffleruby-test-$c . --build-arg GRAALVM_VERSION=$GRAALVM_VERSION --build-arg TEST_BRANCH=master
  popd
done

for c in oraclelinux ubuntu fedora; do
  pushd tool/docker/$c
  docker build -t truffleruby-dev-$c .
  popd
done
