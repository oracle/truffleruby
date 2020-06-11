#!/usr/bin/env bash

SELF_PATH=$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")
while [ -h "$SELF_PATH" ]; do
  DIR=$(dirname "$SELF_PATH")
  SYM=$(readlink "$SELF_PATH")
  SELF_PATH=$(cd "$DIR" && cd "$(dirname "$SYM")" && pwd)/$(basename "$SYM")
done

ecosystem=$(dirname "$SELF_PATH")
cd "$ecosystem/blog6" || exit 1
# shellcheck source=test/truffle/ecosystem/blog-shared.sh.inc
source "$ecosystem/blog-shared.sh.inc"