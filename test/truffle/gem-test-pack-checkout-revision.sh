#!/usr/bin/env bash

set -e
set -x

revision=c5d7d7606459963de4b71b4c31134c3d4eb4314b

git -C ../jruby-truffle-gem-test-pack checkout ${revision}
