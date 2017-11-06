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

function get_libext {
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
root_parent=$(dirname "$root")

# TODO (pitr-ch 01-Mar-2017): investigate if we still need it
if [ -n "$RUBY_BIN" ]; then
    exec "$RUBY_BIN" "$@"
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

# Truffle
binary_truffle="$root/mx.imports/binary/truffle/mxbuild"
source_truffle="$root_parent/graal/truffle/mxbuild"
if [ -f "$binary_truffle/dists/truffle-api.jar" ]; then # Binary Truffle suite
    truffle="$binary_truffle"
    graal_sdk="$(dirname "$binary_truffle")/mx.imports/binary/sdk/mxbuild/dists/graal-sdk.jar"
elif [ -f "$source_truffle/dists/truffle-api.jar" ]; then # Source Truffle suite
    truffle="$source_truffle"
    graal_sdk="$root_parent/graal/sdk/mxbuild/dists/graal-sdk.jar"
else
    echo "Could not find Truffle jars" 1>&2
    exit 1
fi
java_args+=("-Xbootclasspath/a:$truffle/dists/truffle-api.jar:$graal_sdk")
CP="$CP:$truffle/dists/truffle-nfi.jar"
CP="$CP:$root/mxbuild/dists/truffleruby-launcher.jar"
CP="$CP:$root/mxbuild/dists/truffleruby.jar"

libext=$(get_libext)
libtrufflenfi_candidates=()
# look in architecture specific mxbuild directories
libtrufflenfi_candidates+=($(ls -1 "$truffle"/*/truffle-nfi-native/bin/libtrufflenfi."$libext" 2> /dev/null || true))
if [ ${#libtrufflenfi_candidates[@]} -eq 0 ]; then
    # fallback to old path
    # TODO (pitr-ch 07-Nov-2017): remove fallback
    libtrufflenfi=$truffle/truffle-nfi-native/bin/libtrufflenfi.$libext
else
    if [ ${#libtrufflenfi_candidates[@]} -eq 1 ]; then
        libtrufflenfi=${libtrufflenfi_candidates[0]} # take the only candidate
    else
        # more candidates, ask mx which one to use
        platform_segment=$(mx ruby_get_os_arch_path_segment)
        libtrufflenfi=$truffle/$platform_segment/truffle-nfi-native/bin/libtrufflenfi.$libext
    fi
fi

if [ ! -f "$libtrufflenfi" ]; then
    echo "libtrufflenfi.$libext not found."
    exit 1
fi

java_args+=("-Dtruffle.nfi.library=$libtrufflenfi")

# Sulong
binary_sulong="$root/mx.imports/binary/sulong"
source_sulong="$root_parent/sulong"
if [ -f "$binary_sulong/build/sulong.jar" ]; then
  sulong_root="$binary_sulong"
elif [ -f "$source_sulong/build/sulong.jar" ]; then
  sulong_root="$source_sulong"
else
  sulong_root=""
fi
if [ -n "$sulong_root" ]; then
  sulong_jar="$sulong_root/build/sulong.jar"
  CP="$CP:$sulong_jar"
  java_args+=("-Dpolyglot.llvm.libraryPath=$sulong_root/mxbuild/sulong-libs")
fi

# no " to split $JAVA_OPTS into array elements
java_opts=($JAVA_OPTS)

print_command="false"

# Extract -cp/-classpath arguments as we need to merge them
while [ ${#java_opts[@]} -gt 0 ]; do
    val="${java_opts[0]}"
    case "$val" in
    -cmd)
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
        print_command="true"
        ;;
    -J-cp|-J-classpath|-J:cp|-J:classpath)
        CP="$CP:$2"
        shift
        ;;
    --jvm.cp=*)
        CP="$CP:${1:9}"
        ;;
    --jvm.classpath=*)
        CP="$CP:${1:16}"
        ;;
    -J:)
        val=-${val:3}
        ;;
    -J*)
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
    org.truffleruby.Main
    "-Xhome=$root"
    "-Xlauncher=$root/bin/truffleruby"
    "${ruby_args[@]}"
)

if [ "$print_command" = "true" ]; then
    echo $ "${full_command[@]}"
fi

exec "${full_command[@]}"
