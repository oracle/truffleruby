#!/usr/bin/env bash
# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1
#
# Contains code modified from JRuby's jruby.bash and licensed under the same EPL1.0/GPL 2.0/LGPL 2.1
# used throughout.

set -e

function libext {
    uname_str=$(uname)
    if [ "$uname_str" = 'Linux' ] || [ "$uname_str" = 'SunOS' ]; then
        echo so
    elif [ "$uname_str" = 'Darwin' ]; then
        echo dylib
    else
        echo "unknown platform $uname_str" 1>&2
        exit 1
    fi
}

# Prefix any option in $java_args with -J:, values are just copied over
function prefix_java_args {
    local result=()
    for opt in "${java_args[@]}"
    do
        case "$opt" in
            -*)
                result+=("-J:${opt}") ;;
            *)
                result+=("$opt") ;;
        esac
    done
    java_args=("${result[@]}")
}

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

# this is graalvm/language/ruby on graalvm
root=$(dirname "$(dirname "$SELF_PATH")")

root_parent=$(dirname "$root")
if [ "$(basename "$root")" = ruby ] && [ "$(basename "$root_parent")" = language ]; then
    graalvm_root="$(dirname "$root_parent")"
    on_graalvm=true
else
    on_graalvm=false

    # TODO (pitr-ch 01-Mar-2017): investigate if we still need it
    if [ -n "$RUBY_BIN" ]; then
        exec "$RUBY_BIN" "$@"
    fi
fi

if [ -n "$TRUFFLERUBY_RESILIENT_GEM_HOME" ]; then
    unset GEM_HOME GEM_PATH GEM_ROOT
fi

if [ -z "$JAVACMD" ]; then
    if [ -z "$JAVA_HOME" ]; then
        JAVACMD='java'
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
fi

declare -a java_args
declare -a ruby_args

CP=""

if [ $on_graalvm = false ]; then

    mx_jar="$root/mxbuild/dists/truffleruby.jar"

    binary_truffle="$root/mx.imports/binary/truffle/mxbuild"
    if [ -f "$binary_truffle/dists/truffle-api.jar" ]; then # Binary Truffle suite
        truffle="$binary_truffle"
        graal_sdk="$(dirname "$binary_truffle")/mx.imports/binary/sdk/mxbuild/dists/graal-sdk.jar"
    else
        for repo_name in graal compiler truffle
        do
            if [ -f "$root_parent/${repo_name}/truffle/mx.truffle/suite.py" ]; then
                if [ -n "$source_truffle" ]; then
                    echo "Found truffle suite in multiple locations: '$source_truffle'" \
                         "and '$root_parent/${repo_name}/truffle/mxbuild'" 1>&2
                    exit 1
                fi
                source_truffle="$root_parent/${repo_name}/truffle/mxbuild"
                graal_sdk="$root_parent/${repo_name}/sdk/mxbuild/dists/graal-sdk.jar"
            fi
        done
        if [ -n "$source_truffle" ] && [ -f "$source_truffle/dists/truffle-api.jar" ]; then # Source Truffle suite
            truffle="$source_truffle"
        fi
    fi
    if [ -z "$truffle" ]; then
        echo "Could not find Truffle jars" 1>&2
        exit 1
    fi
    java_args+=("-Xbootclasspath/a:$truffle/dists/truffle-api.jar:$graal_sdk")
    # This hash is Truffle's JLINE sha1
    CP="$CP:$HOME/.mx/cache/JLINE_9504d5e2da5d78237239c5226e8200ec21182040.jar"
    CP="$CP:$truffle/dists/truffle-debug.jar:$truffle/dists/truffle-nfi.jar:$mx_jar"
    java_args+=("-Dtruffle.nfi.library=$truffle/truffle-nfi-native/bin/libtrufflenfi.$(libext)")

fi

java_opts=($JAVA_OPTS) # no " to split $JAVA_OPTS into array elements

# Extract classpath options from JAVA_OPTS
for (( i = 0; i < "${#java_opts[@]}"; i++ )); do
    case "${java_opts[$i]}" in
        -cp|-classpath)
            CP="$CP:${java_opts[$i+1]}"
            unset 'java_opts[i]'
            unset 'java_opts[i+1]'
            (( i++ ))
            ;;
    esac
done
# add remaining none classpath options to java_args
java_args+=("${java_opts[@]}")

while [ $# -gt 0 ]
do
    case "$1" in
    -J*)
        val=${1:2}
        if [ "${val:0:1}" = ":" ]; then # -J:
            val=-${val:1}
        fi
        if [ "${val}" = "-classpath" ]; then
            CP="$CP:$2"
            shift
        elif [ "${val}" = "-cp" ]; then
            CP="$CP:$2"
            shift
        else
            java_args=("${java_args[@]}" "$val")
        fi
        ;;
     # Match switches that take an argument
     -C|-e|-I|-S) ruby_args=("${ruby_args[@]}" "$1" "$2"); shift ;;
     # Abort processing on the double dash
     --) break ;;
     # Other opts go to ruby
     -*) ruby_args=("${ruby_args[@]}" "$1") ;;
     # Abort processing on first non-opt arg
     *) break ;;
    esac
    shift
done

print_command=""
for opt in "${java_args[@]}"; do
    [ "${opt}" = "-cmd" ] && print_command="true"
done

# Append the rest of the arguments
ruby_args=("${ruby_args[@]}" "$@")

if [ -n "$CP" ]; then
    # Add classpath to java_args without leading :
    java_args=("-cp" "${CP:1}" "${java_args[@]}")
fi

declare -a full_command
if [ $on_graalvm = false ]; then
    java_args_without_cmd=()
    for value in "${java_args[@]}"; do
        [[ "$value" != "-cmd" ]] && java_args_without_cmd+=("$value")
    done
    full_command=(
        "$JAVACMD"
        "${java_args_without_cmd[@]}"
    )
else
    prefix_java_args
    full_command=(
        "$graalvm_root/bin/graalvm"
        "${java_args[@]}"
        --
    )
fi

full_command=(
    "${full_command[@]}"
    org.truffleruby.Main
    "-Xhome=$root"
    "-Xlauncher=$root/bin/truffleruby"
    "${ruby_args[@]}"
)

if [ -n "$print_command" ]; then
    echo $ "${full_command[@]}"
fi

exec "${full_command[@]}"
