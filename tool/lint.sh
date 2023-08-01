#!/bin/bash

set -e

for f in src/main/c/cext/*.c spec/ruby/optional/capi/ext/*.c
do
  if [[ "$f" == "src/main/c/cext/st.c" ]]; then
   continue
  fi

  bad=$(ruby -e 'puts STDIN.read.scan /^.+\)\s*\n\s*\{/' < "$f" || exit 0)
  if [ -n "$bad" ]; then
    echo "Error in $f"
    echo "The function definition opening brace should be on the same line: ...args) {"
    echo "$bad"
    exit 1
  fi

  bad=$(grep -E '\)\{' "$f" || exit 0)
  if [ -n "$bad" ]; then
    echo "Error in $f"
    echo "There should be a space between ) and {"
    echo "$bad"
    exit 1
  fi

  bad=$(grep -E '\bif\(' "$f" || exit 0)
  if [ -n "$bad" ]; then
    echo "Error in $f"
    echo "There should be a space between if and ("
    echo "$bad"
    exit 1
  fi

  bad=$(grep -E "$(printf '\t')" "$f" || exit 0)
  if [ -n "$bad" ]; then
    echo "Error in $f"
    echo "There should be no tabs in $f"
    echo "$bad"
    exit 1
  fi
done