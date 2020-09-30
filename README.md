![TruffleRuby logo](logo/png/truffleruby_logo_horizontal_medium.png)

TruffleRuby is the [GraalVM](http://graalvm.org/) high-performance implementation
of the [Ruby programming language](https://www.ruby-lang.org/en/) built by
[Oracle Labs](https://labs.oracle.com).

## Getting Started

There are three ways to install TruffleRuby releases and nightly builds:

* Via [GraalVM](doc/user/installing-graalvm.md), which includes support for
  other languages such as JavaScript, R and Python and supports both the
  [*native* and *JVM* configurations](#truffleruby-runtime-configurations).
  Inside GraalVM will then be a `bin/ruby` command that runs TruffleRuby.
  We recommend that you use a [Ruby manager](doc/user/ruby-managers.md#configuring-ruby-managers-for-the-full-graalvm-distribution)
  to use TruffleRuby inside GraalVM.

* Via your [Ruby manager/installer](doc/user/ruby-managers.md) (RVM, rbenv,
  chruby, ruby-build, ruby-install). This contains only TruffleRuby, in the
  [*native* configuration](#truffleruby-runtime-configurations), making it a smaller
  download. It is meant for users just wanting a Ruby implementation and already
  using a Ruby manager.

* Using the [standalone distribution](doc/user/standalone-distribution.md)
  as a simple binary tarball.

We recommend trying TruffleRuby nightly builds which contain the latest fixes and improvements:

```bash
RVM:    $ rvm install truffleruby-head
rbenv:  $ rbenv install truffleruby-dev
chruby: $ ruby-build truffleruby-dev ~/.rubies/truffleruby-dev
```

See the [Ruby installers](doc/user/ruby-managers.md) documentation for more details.

Testing TruffleRuby in CI is easy:
on [TravisCI](https://docs.travis-ci.com/user/languages/ruby#truffleruby), you can use:
```yaml
language: ruby
rvm:
  - truffleruby # or truffleruby-head
```
And on GitHub Actions:
```yaml
- uses: ruby/setup-ruby@v1
  with:
    ruby-version: truffleruby # or truffleruby-head
```
See [Testing TruffleRuby in CI](doc/user/standalone-distribution.md) for more details and other CIs.

You can use `gem` and `bundle` to install Gems as normal.

Please report any issue you might find on [GitHub](https://github.com/oracle/truffleruby/issues).

## Aim

TruffleRuby aims to:

* Run idiomatic Ruby code faster
  * TruffleRuby is the fastest Ruby implementation for many CPU-intensive benchmarks.
* Run Ruby code in parallel
  * TruffleRuby does not have a global interpreter lock and runs Ruby code in parallel.
* Support C extensions
  * Many C extensions work out of the box, including database drivers.
* Add fast and low-overhead interoperability with languages like Java, JavaScript, Python and R
  * Provided by GraalVM, see the [Polyglot documentation](doc/user/polyglot.md).
* Provide new tooling such as debuggers and monitoring that work across languages
  * Includes a profiler, debugger, VisualVM, and more, see the [Tools documentation](doc/user/tools.md).
* All while maintaining very high compatibility with the standard implementation of Ruby

## TruffleRuby Runtime Configurations

There are two main runtime configurations of TruffleRuby: *Native* and *JVM* which make different trade-offs.

| Configuration: | Native (`--native`, default) | JVM (`--jvm`) |
| ------------------ | ------------: | ------------: |
| Time to start TruffleRuby | about as fast as MRI startup | slower |
| Time to get to peak performance | faster | slower |
| Peak performance (also considering GC) | good | best |
| Java host interoperability | needs reflection configuration | just works |

To find out which runtime configuration is used, run `ruby --version` on the command line
or check the value of `RUBY_DESCRIPTION` or `TruffleRuby.native?` in Ruby code.
Runtime configurations are further detailed in [Deploying TruffleRuby](doc/user/deploying.md).

## System Compatibility

TruffleRuby is actively tested on these systems:

* Oracle Linux 7
* Ubuntu 18.04 LTS
* Ubuntu 16.04 LTS
* Fedora 28
* macOS 10.14 (Mojave)
* macOS 10.15 (Catalina)

Architectures:

* AMD64 (aka `x86_64`): Supported
* AArch64 (aka `arm64`): Experimental, C extensions do not work yet on AArch64

You may find that TruffleRuby will not work if you severely restrict the
environment, for example by unmounting system filesystems such as `/dev/shm`.

## Dependencies

* [make and gcc](doc/user/installing-llvm.md) for building C and C++ extensions.
* [libssl](doc/user/installing-libssl.md) for the `openssl` C extension
* [zlib](doc/user/installing-zlib.md) for the `zlib` C extension

Without these dependencies, many libraries including RubyGems will not work.
TruffleRuby will try to print a nice error message if a dependency is missing,
but this can only be done on a best effort basis.

You may also need to set up a [UTF-8 locale](doc/user/utf8-locale.md).

See the [contributor workflow](doc/contributor/workflow.md) document if you wish to build TruffleRuby from source.

## Current Status

We recommend that people trying TruffleRuby on their gems and applications
[get in touch with us](#contact) for help.

TruffleRuby can run Rails and is compatible with many gems, including C extensions.
TruffleRuby is not 100% compatible with MRI 2.6 yet though, please report any compatibility issue you might find.
TruffleRuby [passes around 97% of ruby/spec](https://eregon.me/blog/2020/06/27/ruby-spec-compatibility-report.html),
more than any other alternative Ruby implementation.

TruffleRuby might not be fast yet on Rails applications and large programs.
Notably, large programs currently take a long time to warmup on TruffleRuby and
this is something the TruffleRuby team is currently working on.
Large programs often involve more performance-critical code and
so there is a higher chance to hit an area of TruffleRuby which has not been optimized yet.

## Releases

TruffleRuby has the same version and is released at the same time as GraalVM.
There is a release every 3 months, see the [release roadmap](https://www.graalvm.org/docs/release-notes/version-roadmap/).
There are additional Critical Patch Update releases which are based on regular releases and include extra security fixes.

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

The best way to get in touch with us is to join the channel `#truffleruby` of the
[GraalVM Slack](https://www.graalvm.org/community/#community-support).
You can also Tweet to [@TruffleRuby](https://twitter.com/truffleruby), or email
benoit.daloze@oracle.com.

Please report security vulnerabilities via the process outlined at [reporting
vulnerabilities
guide](https://www.oracle.com/corporate/security-practices/assurance/vulnerability/reporting.html),
rather than by something public such as a GitHub issue or a Gitter
conversation.

## Mailing List

Announcements about GraalVM, including TruffleRuby, are made on the
[graal-dev](http://mail.openjdk.java.net/mailman/listinfo/graal-dev) mailing list.

## Authors

The main authors of TruffleRuby ordered by first contribution are:
Chris Seaton, Benoit Daloze, Kevin Menard, Petr Chalupa, Brandon Fish, Duncan
MacGregor, Christian Wirth, Rafael França, Alan Wu, Nicolas Laurent, Carol Chen.

## Security

See [SECURITY.md](SECURITY.md) for how to report security vulnerabilities to Oracle.
For known vulnerabilities in Ruby, please refer to the [known-cves.md](doc/user/known-cves.md) file.

## Licence

TruffleRuby is copyright (c) 2013-2019 Oracle and/or its affiliates, and is made
available to you under the terms of any one of the following three licenses:

* Eclipse Public License version 2.0, or
* GNU General Public License version 2, or
* GNU Lesser General Public License version 2.1.

See [LICENCE.md](LICENCE.md), [3rd_party_licenses.txt](3rd_party_licenses.txt) and
[doc/legal/legal.md](doc/legal/legal.md).

## Attribution

TruffleRuby is a fork of [JRuby](https://github.com/jruby/jruby), combining it
with code from the [Rubinius](https://github.com/rubinius/rubinius) project, and
also containing code from the standard implementation of Ruby,
[MRI](https://github.com/ruby/ruby).
