#!/usr/bin/env bash

source test/truffle/common.sh.inc

ruby_version=$(jt ruby -v)

if [[ $ruby_version =~ "Native" ]]; then
  echo "The strict encoding checks test can only be run on JVM (the system property value is fixed in Native)"
  exit 0
fi

jt test fast :all -- --vm.Dtruffle.strings.debug-strict-encoding-checks=true --vm.Dtruffle.strings.debug-non-zero-offset-arrays=true
jt test mri test/mri/tests/ruby/test_string* test/mri/tests/ruby/test_m17n* test/mri/tests/ruby/enc --vm.Dtruffle.strings.debug-strict-encoding-checks=true --vm.Dtruffle.strings.debug-non-zero-offset-arrays=true
