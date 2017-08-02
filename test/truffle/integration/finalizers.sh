#!/usr/bin/env bash

source test/truffle/common.sh.inc

for [f in test/truffle/integration/finalizers/*.rb]; do
  jt ruby $f
done
