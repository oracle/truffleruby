# Using TruffleRuby with GraalVM

[GraalVM](http://graalvm.org/) is the platform on which TruffleRuby runs.

## Dependencies

[TruffleRuby's dependencies](../../README.md#dependencies) need to be installed
for TruffleRuby to run correctly.

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
high-performance language runtimes.

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

```bash
$ gu install ruby
```

This command will show a message mentioning to run a post-install script.
This is necessary to make the Ruby openssl C extension work with your system libssl.
Please run that script now.

You can also download the Ruby component (`ruby-installable-...`) manually from
https://github.com/oracle/truffleruby/releases. Then install it with
`gu install --file path/to/ruby-installable-...`.

If you are installing Ruby into GraalVM EE then you need to download the Ruby
EE installable from OTN and install using `--file` in the same way.

After installing Ruby you may want to rebuild other images so that they can
use the new language. Rebuilding the executable images can take a few minutes
and you should have about 10 GB of RAM available.

```bash
$ gu rebuild-images polyglot libpolyglot
```

## Using a Ruby package manager

Inside the GraalVM is a `jre/languages/ruby` directory which has the usual
structure of a Ruby implementation. It is recommended to add this directory to
a Ruby manager, see [configuring Ruby managers](ruby-managers.md) for more
information.
