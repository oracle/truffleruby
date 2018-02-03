#!/bin/sh

docker rmi $(docker images -q)
docker rm $(docker ps -a -q)

for c in oraclelinux ubuntu fedora rbenv chruby rvm; do
  pushd test/truffle/docker/$c
  docker build -t truffleruby-test-$c . --build-arg GRAALVM_VERSION=0.31-dev --build-arg TEST_BRANCH=master
  popd
done

for c in oraclelinux ubuntu fedora; do
  pushd test/truffle/docker/$c
  docker build -t truffleruby-dev-$c .
  popd
done
