#!/usr/bin/env bash

set -e
set -x

revision=8dcd309a616a560557965a497b06bbea6581d33c

git -C ../jruby-truffle-gem-test-pack checkout ${revision}
