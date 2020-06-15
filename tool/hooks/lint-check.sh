#!/usr/bin/env bash

# Set this up by before each commit with
#   cp tool/hooks/lint-check.sh .git/hooks/pre-commit
# or before each push with
#   cp tool/hooks/lint-check.sh .git/hooks/pre-push
# The choice is yours.

exec tool/jt.rb lint fast
