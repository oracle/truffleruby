![TruffleRuby logo](logo/png/truffleruby_logo_horizontal_medium.png)

TruffleRuby is the [GraalVM](http://graalvm.org/) high-performance implementation
of the [Ruby programming language](https://www.ruby-lang.org/en/).

## Getting Started

There are four ways to install TruffleRuby releases and nightly builds:

* Via [GraalVM](doc/user/installing-graalvm.md), which includes support for
  other languages such as JavaScript, R, and Python, and supports both the
  [Native and JVM configurations](#truffleruby-runtime-configurations).
  Inside GraalVM will then be a `bin/ruby` command that runs TruffleRuby.
  We recommend that you use a [Ruby manager](doc/user/ruby-managers.md#configuring-ruby-managers-for-the-full-graalvm-distribution)
  to use TruffleRuby inside GraalVM.

* Via your [Ruby manager/installer](doc/user/ruby-managers.md) (RVM, rbenv,
  chruby, ruby-build, ruby-install). This contains only TruffleRuby, in the
  [Native configuration](#truffleruby-runtime-configurations), making it a smaller
  download. It is meant for users who only want a Ruby implementation and are already
  using a Ruby manager.

* Via Docker: [Oracle Linux-based images](https://github.com/graalvm/container/pkgs/container/truffleruby)
  and [Debian-based images](https://hub.docker.com/r/flavorjones/truffleruby).

* Via the [standalone distribution](doc/user/standalone-distribution.md)
  as a simple binary tarball.

We recommend trying TruffleRuby nightly builds which contain the latest fixes and improvements:

```bash
RVM:    $ rvm install truffleruby-head
rbenv:  $ rbenv install truffleruby-dev
chruby: $ ruby-build truffleruby-dev ~/.rubies/truffleruby-dev
```

See the [Ruby installers](doc/user/ruby-managers.md) documentation for more details.

Testing TruffleRuby in CI is easy.
On GitHub Actions, you can use:

```yaml
- uses: ruby/setup-ruby@v1
  with:
    ruby-version: truffleruby # or truffleruby-head
```

See [Testing TruffleRuby in CI](doc/user/standalone-distribution.md) for more details and other CIs.

You can use `gem` and `bundle` to install gems, as usual.

Please report any issues you might find on [GitHub](https://github.com/oracle/truffleruby/issues).

## Aim

TruffleRuby aims to:

* Run idiomatic Ruby code faster.
  * TruffleRuby is the fastest Ruby implementation for many CPU-intensive benchmarks.
* Run Ruby code in parallel.
  * TruffleRuby does not have a global interpreter lock and runs Ruby code in parallel.
* Support C extensions.
  * Many C extensions work out of the box, including database drivers.
* Add fast and low-overhead interoperability with languages like Java, JavaScript, Python, and R.
  * Provided by GraalVM, see the [Polyglot documentation](doc/user/polyglot.md).
* Provide new tooling, such as debuggers and monitoring, that works across languages.
  * Includes a profiler, debugger, VisualVM, and more. See the [Tools documentation](doc/user/tools.md).
* Provide all of the above while maintaining very high compatibility with the standard implementation of Ruby.

## TruffleRuby Runtime Configurations

There are two main runtime configurations of TruffleRuby, Native and JVM, which have different trade-offs.

| Configuration: | Native (`--native`, default) | JVM (`--jvm`) |
| ------------------ | ------------: | ------------: |
| Time to start TruffleRuby | about as fast as MRI startup | slower |
| Time to reach peak performance | faster | slower |
| Peak performance (also considering GC) | good | best |
| Java host interoperability | needs reflection configuration | just works |

To find out which runtime configuration is being used, run `ruby --version` on the command line,
or check the value of `RUBY_DESCRIPTION` or `TruffleRuby.native?` in Ruby code.
Runtime configurations are further detailed in [Deploying TruffleRuby](doc/user/deploying.md).

## System Compatibility

TruffleRuby is actively tested on the following systems:

* Oracle Linux 7
* Ubuntu 18.04 LTS
* Ubuntu 16.04 LTS
* Fedora 28
* macOS 10.14 (Mojave)
* macOS 10.15 (Catalina)

Architectures:

* AMD64 (aka `x86_64`): Supported
* AArch64 (aka `arm64`): Supported on Linux (from 20.2)

You may find that TruffleRuby will not work if you severely restrict the
environment, for example, by unmounting system filesystems such as `/dev/shm`.

## Dependencies

* [make and gcc](doc/user/installing-llvm.md) for building C and C++ extensions
* [libssl](doc/user/installing-libssl.md) for the `openssl` C extension
* [zlib](doc/user/installing-zlib.md) for the `zlib` C extension

Without these dependencies, many libraries including RubyGems will not work.
TruffleRuby will try to print a nice error message if a dependency is missing, but this can only be done on a best effort basis.

You may also need to set up a [UTF-8 locale](doc/user/utf8-locale.md).

See the [contributor workflow](doc/contributor/workflow.md) document if you wish to build TruffleRuby from source.

## Current Status

We recommend that people trying TruffleRuby on their gems and applications [get in touch with us](#contact) for help.

TruffleRuby can run Rails and is compatible with many gems, including C extensions.
However, TruffleRuby is not 100% compatible with MRI 2.7 yet. Please report any compatibility issues you might find.
TruffleRuby [passes around 97% of ruby/spec](https://eregon.me/blog/2020/06/27/ruby-spec-compatibility-report.html),
more than any other alternative Ruby implementation.

TruffleRuby might not be fast yet on Rails applications and large programs.
Notably, large programs currently take a long time to warmup on TruffleRuby and
this is something the TruffleRuby team is currently working on.
Large programs often involve more performance-critical code
so there is a higher chance of hitting an area of TruffleRuby which has not been optimized yet.

## Releases

TruffleRuby has the same version, and is released at the same time as GraalVM.
There is a release every 3 months. See the [release roadmap](https://www.graalvm.org/release-notes/version-roadmap/).

## Migration from MRI

TruffleRuby should in most cases work as a drop-in replacement for MRI, but you
should read about our [compatibility](doc/user/compatibility.md).

## Migration from JRuby

For many use cases TruffleRuby should work as a drop-in replacement for JRuby.
However, our approach to integration with Java is different to JRuby so you
should read our [migration guide](doc/user/jruby-migration.md).

## Documentation

Extensive user documentation is available in [`doc/user`](doc/user).

See our [source code repository](https://github.com/oracle/truffleruby) and
[contributor documentation](CONTRIBUTING.md) to contribute to TruffleRuby.
In particular, see the [contributor workflow](doc/contributor/workflow.md)
document for how to build and run TruffleRuby.

## Contact

The best way to get in touch with us is to join the `#truffleruby` channel on
[GraalVM Slack](https://www.graalvm.org/community/#community-support).
You can also Tweet to [@TruffleRuby](https://twitter.com/truffleruby), or email
_benoit.daloze@oracle.com_.

Please report security vulnerabilities via the process outlined in the [reporting vulnerabilities guide](https://www.oracle.com/corporate/security-practices/assurance/vulnerability/reporting.html), rather than by something public such as a GitHub issue or a Gitter conversation.

## Mailing List

Announcements about GraalVM, including TruffleRuby, are made on the
[graal-dev](http://mail.openjdk.java.net/mailman/listinfo/graal-dev) mailing list.

## Authors

The main authors of TruffleRuby ordered by first contribution are:
Chris Seaton, Benoit Daloze, Kevin Menard, Petr Chalupa, Brandon Fish, Duncan MacGregor, Christian Wirth, Rafael França, Alan Wu, Nicolas Laurent, Carol Chen, Nikolay Sverchkov, and Lillian Zhang.

## Security

See [SECURITY](SECURITY.md) for how to report security vulnerabilities to Oracle.
For known vulnerabilities in Ruby, please refer to the [known-cves](doc/user/known-cves.md) file.

## Licence

TruffleRuby is copyright (c) 2013-2021 Oracle and/or its affiliates, and is made
available to you under the terms of any one of the following three licenses:

* Eclipse Public License version 2.0, or
* GNU General Public License version 2, or
* GNU Lesser General Public License version 2.1.

For further licensing information, see [LICENCE](LICENCE.md), [3rd_party_licenses](3rd_party_licenses.txt), and [doc/legal/legal](doc/legal/legal.md).

## Attribution

TruffleRuby includes infrastructure code from [JRuby](https://github.com/jruby/jruby) (e.g. parser, JCodings, Joni), core library code from the [Rubinius](https://github.com/rubinius/rubinius) project, as well as code from the standard implementation of Ruby, [MRI](https://github.com/ruby/ruby).
