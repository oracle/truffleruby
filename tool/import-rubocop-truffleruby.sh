#!/usr/bin/env bash

# This file should pass `shellcheck tool/import-rubocop-truffleruby.sh`.

set -x
set -e

SOURCE_DIR="../rubocop-truffleruby"
SOURCE_PATH="lib/rubocop/cop/truffleruby"
TARGET_DIR="tool/rubocop-truffleruby"
GIT_REPO_URL="git@github.com:andrykonchin/rubocop-truffleruby.git"

if [ ! -d "$SOURCE_DIR" ]; then
  mkdir -p $SOURCE_DIR
fi

# clone the repository if the SOURCE_DIR is empty
if [ ! "$(ls $SOURCE_DIR)" ]; then
  if ! git clone $GIT_REPO_URL $SOURCE_DIR; then
    echo "FAILURE: git clone failed"
  fi
fi
cp $SOURCE_DIR/$SOURCE_PATH/* $TARGET_DIR/cop/