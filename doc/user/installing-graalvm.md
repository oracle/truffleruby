---
layout: docs-experimental
toc_group: ruby
link_title: Using Ruby with GraalVM
permalink: /reference-manual/ruby/InstallingGraalVM/
---
# Using TruffleRuby with GraalVM

[GraalVM](http://graalvm.org/) is the platform on which TruffleRuby runs.

Installing GraalVM enables you to run TruffleRuby both in the `--native` and `--jvm` [runtime configurations](../../README.md#truffleruby-runtime-configurations).

## Dependencies

[TruffleRuby's dependencies](../../README.md#dependencies) need to be installed for TruffleRuby to run correctly.

## Community Edition and Enterprise Edition

GraalVM is available in a Community Edition, which is open-source, and an Enterprise Edition which has better performance and scalability.
See [the website](https://www.graalvm.org/downloads) for a comparison.

## Installing the Base Image

GraalVM starts with a base image which provides the platform for high-performance language runtimes.

The Community Edition base image can be installed [from GitHub](https://www.graalvm.org/downloads), under an open source licence.

The Enterprise Edition base image can be installed from [Oracle Downloads](https://www.oracle.com/downloads/graalvm-downloads.html) page by accepting the Oracle License Agreement.

Nightly builds of the GraalVM Community Edition are [also available](https://github.com/graalvm/graalvm-ce-dev-builds/releases).

Whichever edition you choose, you will obtain a tarball which you can extract.
There will be a `bin` directory (`Contents/Home/bin` on macOS) which you can add to your `$PATH` if you want to.

### Installing with asdf

Using [asdf](https://github.com/asdf-vm/asdf) and [asdf-java](https://github.com/halcyon/asdf-java) installation is as easy as
`asdf install java graalvm-20.1.0+java11` (look up versions via `asdf list-all java | grep graalvm`).

## Installing Ruby and Other Languages

After installing GraalVM you then need to install the Ruby language into it.
This is done using the `gu` command.
The Ruby package is the same for both editions of GraalVM and comes from GitHub:
```bash
gu install ruby
```

This command will show a message regarding running a post-install script.
This is necessary to make the Ruby `openssl` C extension work with your system libssl.
Please run that script now.
The path of the script will be:
```bash
languages/ruby/lib/truffle/post_install_hook.sh
```

You can also download the latest Ruby component (`ruby-installable-...`) manually from [GitHub](https://github.com/oracle/truffleruby/releases/latest) (CE)
or from [Oracle Downloads](https://www.oracle.com/downloads/graalvm-downloads.html) (EE).
Then install it with `gu install --file path/to/ruby-installable-...`.

## Using a Ruby Manager

Inside GraalVM is a `jre/languages/ruby` or `languages/ruby` directory which has the usual structure of a Ruby implementation. It is recommended to add this directory to a Ruby manager.
See [configuring Ruby managers](ruby-managers.md) for more information.
