#!/usr/bin/env bash

# Set this up before each commit with
#   cp tool/hooks/lint-check.sh .git/hooks/pre-commit
# or after each commit with
#   cp tool/hooks/lint-check.sh .git/hooks/post-commit
# or before each push with
#   cp tool/hooks/lint-check.sh .git/hooks/pre-push
# The choice is yours.

exec tool/jt.rb lint fast
