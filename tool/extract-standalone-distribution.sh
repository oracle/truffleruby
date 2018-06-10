#!/bin/bash

set -e
set -x

file="$1"
# e.g.: 1.0.0-rc2
version="$2"

case "$(basename $file)" in
  *linux-amd64*) os_arch="linux-amd64" ;;
  *macos-amd64*) os_arch="macos-amd64" ;;
  *)
    echo "cannot find platform in $file" 1>&2
    exit 1
    ;;
esac

rm -rf tmp
mkdir tmp

cd tmp
jar xf "$file"
cd ..

ruby tool/restore-perms-symlinks.rb tmp

# The archive basename should be inferable from the version and platform,
# so that Ruby installers know how to find the archive of a given version.
archive_basename="truffleruby-$version-$os_arch"

rm -rf "$archive_basename"
cp -R -p tmp/jre/languages/ruby "$archive_basename"

# Remove unused files in a native-only distribution
rm "$archive_basename/native-image.properties"
rm "$archive_basename"/*.jar

# Create archive
archive_name="$archive_basename.tar.gz"
tar czf "$archive_name" "$archive_basename"
