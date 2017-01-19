#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt ruby -Xplatform.use_java=true -e 'puts 14'
