#!/usr/bin/env bash

set -e
set -x

revision=0887964ceb6e9446a4854cac54da28b4e83b97e4

git -C ../jruby-truffle-gem-test-pack checkout ${revision}
