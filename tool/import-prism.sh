#!/usr/bin/env bash

set -x
set -e

YARP=../../prism

export PRISM_SERIALIZE_ONLY_SEMANTICS_FIELDS=1

# Create generated files
pushd $YARP
bundle
bundle exec rake clobber
bundle exec rake templates
popd

rm -rf src/main/c/yarp
mkdir src/main/c/yarp
cp -R $YARP/{include,src} src/main/c/yarp
cp $YARP/{LICENSE.md,Makefile} src/main/c/yarp

rm -rf src/yarp/java
cp -R $YARP/java src/yarp/java
