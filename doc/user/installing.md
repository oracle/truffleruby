# Using TruffleRuby with GraalVM

[GraalVM](http://graalvm.org/) is the platform on which TruffleRuby runs.

## Dependencies

TruffleRuby is actively tested on these systems:

* Oracle Linux 7
* Ubuntu 16.04 LTS
* Fedora 25
* macOS 10.13

You need to [install LLVM](doc/user/installing-llvm.md) to build and run C
extensions and [`zlib`](doc/user/installing-zlib.md) and
[`libssl`](doc/user/installing-libssl.md) for `openssl`. You may also need to
set up a [UTF-8 locale](doc/user/utf8-locale.md).

## Community Edition and Enterprise Edition

GraalVM is available in a Community Edition, which is open-source, and an
Enterprise Edition which has better performance and scalability.

The Community Edition is available only for Linux, but is free for production
use. The Enterprise Edition is available for both macOS and Linux, and is free
for evaluation but not production use. Commercial support is available for the
Enterprise Edition.

To get the best performance you want to use the Enterprise Edition.

## Installing the base image

GraalVM starts with a base image which provides the platform for
high-performance scalability.

The Community Edition base image can be installed from GitHub, under an open
source licence.

https://github.com/oracle/graal/releases

The Enterprise Edition base image can only be installed from the Oracle
Technology Network using the OTN licence.

http://www.oracle.com/technetwork/oracle-labs/program-languages/

Whichever edition you get you will get a tarball which you can extract. There
will be a `bin` directory (`Contents/Home/bin` on macOS) which you can add to
your `$PATH` if you want to.

## Installing Ruby and other languages

After installing GraalVM you then need to install the Ruby language into it.
This is done using the `gu` command. The Ruby package is the same for both
editions of GraalVM and comes from GitHub.

```
$ gu install ruby
```

Or download manually from https://github.com/oracle/truffleruby/releases.

If you install Ruby into the Enterprise Edition of GraalVM, you may then want to
rebuild the Ruby executable images using the runtime from the Enterprise
Edition. The version of the Ruby executable images you install by default uses
the Community Edition runtime until you rebuild.

To get the best performance you want to rebuild the images.

Rebuilding the executable images can take a few minutes.

```
$ graalvm/jre/lib/svm/bin/rebuild-images ruby
```

## Using a Ruby package manager

Inside the GraalVM is a `jre/languages/ruby` directory which has the usual
structure of a Ruby implementation. It is recommended to add this directory to
a Ruby manager, see [configuring Ruby managers](ruby-managers.md) for more
information.
