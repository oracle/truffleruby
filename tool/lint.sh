#!/bin/bash

set -e

bad=$(egrep -r ',\s*@Cached' truffleruby/src || exit 0)

if [ -n "$bad" ]; then
	echo "@Cached arguments should be on their own line"
	echo "$bad"
	exit 1
fi
