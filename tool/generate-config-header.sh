#!/bin/bash

set -e
set -x

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

os=$(uname -s)
os=${os/Linux/linux}
os=${os/Darwin/darwin}

arch=$(uname -m)
arch=${arch/x86_64/amd64}

mx_platform="${os}_${arch}"

cp .ext/include/*/ruby/config.h "../lib/cext/include/truffleruby/config_${mx_platform}.h"
