#!/usr/bin/env bash

source test/truffle/common.sh.inc

ruby_version=$(jt ruby -v)

if [[ $ruby_version =~ "Native" ]]; then
  echo "The strict encoding checks test can only be run on JVM (the system property value is fixed in Native)"
  exit 0
fi

jt test fast :all -- --vm.Dtruffle.strings.debug-strict-encoding-checks=true
