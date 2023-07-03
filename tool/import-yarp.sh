#!/usr/bin/env bash

set -x
set -e

YARP=../../yarp

# Create generated files
pushd $YARP
bundle
bundle exec rake clobber
bundle exec rake templates
bundle exec rake configure
popd

rm -rf src/main/c/yarp
mkdir src/main/c/yarp
cp -R $YARP/{include,src} src/main/c/yarp
cp $YARP/{.gitignore,LICENSE.md,configure,config.h.in,Makefile.in} src/main/c/yarp

rm -rf src/yarp/java
cp -R $YARP/java src/yarp/java
