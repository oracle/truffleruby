#!/usr/bin/env bash

source test/truffle/common.sh.inc

rm -f ruby.log

jt ruby -J-Djava.util.logging.config.file=doc/samples/logging.properties -e 14

if [ ! -f ruby.log ]
then
    rm -f ruby.log
    echo No log file produced
    exit 1
fi

if ! grep 'ruby home' ruby.log > /dev/null
then
    rm -f ruby.log
    echo Log not as expected
    exit 1
fi

rm -f ruby.log
