#!/usr/bin/env bash

source test/truffle/common.sh.inc

# Clone Graal.js repository
jt mx --dy /vm,/graal-js sversions

jt mx --env jvm-js ruby_maven_deploy_public
maven_repo="$(dirname "$(pwd)")/graal/sdk/mxbuild/jdk21/mx.sdk/public-maven-repo"
if [ ! -d "$maven_repo" ]; then
    echo "Maven repo not at $maven_repo ?"
    exit 2
fi

jt build --env jvm-ce

standalone=$(jt -q mx --quiet --env jvm-ce standalone-home --type=jvm ruby)

export PATH="$standalone/bin:$PATH"

env org.graalvm.maven.downloader.repository="file://$maven_repo" truffleruby-polyglot-get js-community

out=$(ruby --polyglot -e 'p Polyglot.eval("js", "1/2")')
if [ "$out" != "0.5" ]; then
    echo "Wrong output: >>$out<<"
    exit 1
fi
