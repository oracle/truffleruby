#!/usr/bin/env bash
# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
#
# Contains code modified from JRuby's jruby.bash and licensed under the same EPL1.0/GPL 2.0/LGPL 2.1
# used throughout.

set -e

# get the absolute path of the executable and resolve symlinks
SELF_PATH=$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")
while [ -h "$SELF_PATH" ]; do
    # 1) cd to directory of the symlink
    # 2) cd to the directory of where the symlink points
    # 3) get the pwd
    # 4) append the basename
    DIR=$(dirname "$SELF_PATH")
    SYM=$(readlink "$SELF_PATH")
    SELF_PATH=$(cd "$DIR" && cd "$(dirname "$SYM")" && pwd)/$(basename "$SYM")
done

root=$(dirname "$(dirname "$SELF_PATH")")
# Used by jvm_args
root_parent=$(dirname "$root")

# TODO (pitr-ch 01-Mar-2017): investigate if we still need it
if [ -n "$RUBY_BIN" ]; then
    exec "$RUBY_BIN" "$@"
fi

# Source values provided by mx at build time
source "$root/mxbuild/jvm_args.sh"

# Source truffleruby_env generated for the distribution
if [ -f "$root/truffleruby_env" ]; then
  source "$root/truffleruby_env"
fi

if [ -z "$JAVACMD" ]; then
    if [ -z "$JAVA_HOME" ]; then
        JAVACMD='java'
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
fi

java_args=()
CP=""

# Increase the Metaspace size to avoid a Full GC during startup,
# triggered by the default MetaspaceSize (~20MB).
java_args+=("-XX:MetaspaceSize=25M")

# Append values from jvm_args
java_args+=("-Xbootclasspath/a:$bootclasspath")
CP="$CP:$classpath"
java_args+=(${properties[@]})

# Set ruby.home in the development lancher,
# so getTruffleLanguageHome() reports the correct directory.
java_args+=("-Druby.home=$root")

# no " to split $JAVA_OPTS into array elements
java_opts=($JAVA_OPTS)

print_command="false"

# Extract -cp/-classpath arguments as we need to merge them
while [ ${#java_opts[@]} -gt 0 ]; do
    val="${java_opts[0]}"
    case "$val" in
    -cmd)
        echo '[ruby]' WARNING -cmd in $JAVA_OPTS has been deprecated and will be removed >&2
        print_command="true"
        ;;
    -cp|-classpath)
        java_opts=("${java_opts[@]:1}")
        CP="$CP:${java_opts[0]}"
        ;;
    *)
        java_args+=("$val")
        ;;
    esac
    java_opts=("${java_opts[@]:1}")
done

ruby_args=()

# Parse command line arguments
while [ $# -gt 0 ]
do
    case "$1" in
    -J-cmd|--jvm.cmd)
        echo '[ruby]' WARNING "$1" has been deprecated and will be removed >&2
        print_command="true"
        ;;
    -J-cp|-J-classpath)
        echo '[ruby]' WARNING "$1" has been deprecated and will be removed - use --jvm.classpath="$2" instead >&2
        CP="$CP:$2"
        shift
        ;;
    --jvm.cp=*)
        CP="$CP:${1:9}"
        ;;
    --jvm.classpath=*)
        CP="$CP:${1:16}"
        ;;
    -J*)
        echo '[ruby]' WARNING "$1" has been deprecated and will be removed - use --jvm."${1:3}" instead >&2
        java_args+=("${1:2}")
        ;;
    --jvm.*)
        java_args+=("-${1:6}")
        ;;
    -C|-e|-I|-S) # Match switches that take an argument
        ruby_args+=("$1" "$2")
        shift
        ;;
    --) # Abort processing on the double dash
        break
        ;;
    -*) # Other options go to ruby
        ruby_args+=("$1")
        ;;
    *) # Abort processing on first non-opt arg
        break
        ;;
    esac
    shift
done

# Append the rest of the arguments
ruby_args+=("$@")

if [ -n "$CP" ]; then
    # Add classpath to java_args without leading :
    java_args=("-cp" "${CP:1}" "${java_args[@]}")
fi

full_command=(
    "$JAVACMD"
    "${java_args[@]}"
    org.truffleruby.launcher.RubyLauncher
    "-Xlauncher=$root/bin/truffleruby"
    "${ruby_args[@]}"
)

if [ "$print_command" = "true" ]; then
    echo $ "${full_command[@]}"
fi

exec "${full_command[@]}"
