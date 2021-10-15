#!/bin/bash

set -e
set -x

if [ -z "$CC" ]; then
    echo "CC must be set"
    exit 1
fi

url="$1"
if [ -z "$url" ]; then
    url=https://cache.ruby-lang.org/pub/ruby/3.0/ruby-3.0.2.tar.gz
fi

archive=$(basename "$url")

if [ ! -e "$archive" ]; then
    curl -O "$url"
    tar xf ruby-3.0.2.tar.gz
fi

cd ruby-3.0.2 || exit 1
./configure || (cat config.log; exit 1)

cruby_platform=$(ruby -e 'puts RUBY_PLATFORM')
mx_platform=$(ruby -e 'puts RUBY_PLATFORM.split("-").reverse.join("_").sub("x86_64", "amd64")')

cp ".ext/include/${cruby_platform}/ruby/config.h" "../lib/cext/include/truffleruby/config_${mx_platform}.h"
