#!/usr/bin/env bash

set -e
set -x

revision=d2de0d405677c27bbf69a464db665351fa626269

git -C ../jruby-truffle-gem-test-pack checkout ${revision}
