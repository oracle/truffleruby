#!/bin/bash

set -e

bad=$(ruby -e 'puts STDIN.read.scan /^.+\)\s*\n\s*\{/' < src/main/c/cext/ruby.c || exit 0)
if [ -n "$bad" ]; then
	echo "The function definition opening brace should be on the same line: ...args) {"
	echo "$bad"
	exit 1
fi

bad=$(egrep '\)\{' src/main/c/cext/ruby.c || exit 0)
if [ -n "$bad" ]; then
	echo "There should be a space between ) and {"
	echo "$bad"
	exit 1
fi

bad=$(egrep '\bif\(' src/main/c/cext/ruby.c || exit 0)
if [ -n "$bad" ]; then
	echo "There should be a space between if and ("
	echo "$bad"
	exit 1
fi
