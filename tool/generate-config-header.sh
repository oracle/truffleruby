#!/bin/bash

set -e
set -x

VERSION=$(cat .ruby-version)

url="$1"
if [ -z "$url" ]; then
    # The source archive, a copy from https://www.ruby-lang.org/en/downloads/
    url=$(mx urlrewrite "https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/ruby-$VERSION.tar.gz")
fi

os=$(uname -s)
os=${os/Linux/linux}
os=${os/Darwin/darwin}

arch=$(uname -m)
arch=${arch/x86_64/amd64}

mx_platform="${os}_${arch}"

if [ "$os" != "darwin" ]; then
    if [ -z "$TOOLCHAIN_PATH" ]; then
        echo "TOOLCHAIN_PATH must be set"
        exit 1
    fi
    export PATH="$TOOLCHAIN_PATH:$PATH"
fi

archive=$(basename "$url")

if [ ! -e "$archive" ]; then
    curl -O "$url"
fi

if [ ! -d "ruby-$VERSION" ]; then
    tar xf "$archive"
fi

cd "ruby-$VERSION" || exit 1
./configure || (cat config.log; exit 1)

cp .ext/include/*/ruby/config.h "../lib/cext/include/truffleruby/config_${mx_platform}.h"
