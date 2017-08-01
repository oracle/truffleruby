#!/bin/bash

set -e

bad=$(egrep -r ',\s*@Cached' src || exit 0)
if [ -n "$bad" ]; then
	echo "@Cached arguments should be on their own line"
	echo "$bad"
	exit 1
fi

bad=$(egrep -r 'computeIfAbsent' src || exit 0)
if [ -n "$bad" ]; then
	echo "Use get() + putIfAbsent() instead of computeIfAbsent() which is not scalable"
	echo "$bad"
	exit 1
fi
