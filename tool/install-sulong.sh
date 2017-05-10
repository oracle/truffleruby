#!/usr/bin/env bash

set -e
set -x

git clone "https://github.com/graalvm/sulong.git" ../sulong
pushd ../sulong
mx sversions
mx build
export SULONG_HOME=$(pwd)
popd

tool/jt.rb build cexts
