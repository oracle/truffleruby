#!/usr/bin/env bash

set -e
set -x

revision=b6f54dfa740a4797dbab1b2d3fdcbf57ec2248fe

git -C ../jruby-truffle-gem-test-pack checkout ${revision}
