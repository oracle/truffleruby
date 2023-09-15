---
layout: docs-experimental
toc_group: ruby
link_title: Installing TruffleRuby
permalink: /reference-manual/ruby/InstallingTruffleRuby/
---
# Installing TruffleRuby

The recommended way to install TruffleRuby is via your Ruby manager/installer, see [Getting Started](../../README.md#getting-started).

You can also install TruffleRuby by manually downloading the binary tarball, extracting it, running the post-install script and adding TruffleRuby to `$PATH`.
This page documents the URLs and the extra steps necessary after downloading the tarball. 

## Dependencies

[TruffleRuby's dependencies](../../README.md#dependencies) need to be installed for TruffleRuby to run correctly.

## Oracle GraalVM and GraalVM Community Edition

There are 2 variants of TruffleRuby and GraalVM:
* Oracle GraalVM, which provides the best TruffleRuby experience: it is significantly faster and more memory-efficient.
* GraalVM Community Edition, which is fully open-source.

Oracle GraalVM is the GraalVM distribution from Oracle available under the [GraalVM Free Terms and Conditions](https://medium.com/graalvm/161527df3d76).

## Download Links

### Native Standalone

Releases of the Native Standalone are [available on GitHub](https://github.com/oracle/truffleruby/releases/latest).
The Native Standalones are the files:
```
# Oracle GraalVM Native Standalone
truffleruby-VERSION-PLATFORM.tar.gz
# GraalVM Community Edition Native Standalone
truffleruby-community-VERSION-PLATFORM.tar.gz
```

Development builds are [also available](https://github.com/ruby/truffleruby-dev-builder/releases/latest).  
More platforms for dev builds are [available here](https://github.com/graalvm/graalvm-ce-dev-builds/releases/latest) but those builds tend to be slightly older.
The Native Standalones are the files `truffleruby-community-dev-PLATFORM.tar.gz`.

### JVM Standalone

Releases of the Native Standalone are [available on GitHub](https://github.com/oracle/truffleruby/releases/latest).
The JVM Standalones are the files:
```
# Oracle GraalVM JVM Standalone
truffleruby-jvm-VERSION-PLATFORM.tar.gz
# GraalVM Community Edition JVM Standalone
truffleruby-community-jvm-VERSION-PLATFORM.tar.gz
```

Development builds are [also available](https://github.com/graalvm/graalvm-ce-dev-builds/releases/latest).
The JVM Standalones are the files `truffleruby-community-jvm-dev-PLATFORM.tar.gz`.

## After Downloading

Once you have downloaded a tarball, extract it.
We will refer to the directory you extracted it as `$EXTRACTED_DIRECTORY`.
This directory should contain `bin/truffleruby`, `lib/truffle`, etc.

Then you need to run the post-install script.
This is necessary to make the Ruby `openssl` C extension work with your system libssl.
The path of the script will be:
```bash
$EXTRACTED_DIRECTORY/lib/truffle/post_install_hook.sh
```

You can then add `$EXTRACTED_DIRECTORY/bin` to `PATH` and use `ruby`/`gem`/`bundle`/etc.

## RubyGems Configuration

Note that you also need to ensure `GEM_HOME` and `GEM_PATH` are not set, so TruffleRuby uses the correct `GEM_HOME` and `GEM_PATH`.
See [Using TruffleRuby without a Ruby manager](ruby-managers.md#using-truffleruby-without-a-ruby-manager) for details.
