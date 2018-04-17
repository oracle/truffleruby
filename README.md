![TruffleRuby logo](logo/png/truffleruby_logo_horizontal_medium.png)

A high performance implementation of the Ruby programming language. Built on
[GraalVM](http://graalvm.org/) by [Oracle Labs](https://labs.oracle.com).

## Getting Started

To get started with TruffleRuby
[install GraalVM and Ruby](doc/user/installing.md). Inside GraalVM will then be
a `bin/ruby` command that runs TruffleRuby.

We recommend that you use a [Ruby version manager](doc/user/ruby-managers.md)
to use TruffleRuby.

You can use `gem` to install Gems as normal. TruffleRuby currently requires
Bundler version `1.16.x`.

You can also build TruffleRuby from source, see the
[Building Instructions](doc/contributor/workflow.md).

## Aim

TruffleRuby aims to:

* Run idiomatic Ruby code faster
* Run Ruby code in parallel
* Boot Ruby applications in less time
* Execute C extensions in a managed environment
* Provide new tooling such as debuggers and monitoring
* Add fast and low-overhead interoperability with languages like JavaScript, Python and R
* All while maintaining very high compatibility with the standard implementation of Ruby

## TruffleRuby Configurations

There are two main configurations of TruffleRuby - *native* and *JVM*. It's
important to understand the different configurations of TruffleRuby, as each has
different capabilities and performance characteristics. You should pick the
execution mode that is appropriate for your application.

When distributed as part of GraalVM, TruffleRuby by default runs in the *native*
configuration. In this configuration, TruffleRuby is ahead-of-time compiled to a
standalone native executable. This means that you don't need a JVM installed on
your system to use it. The advantage of the native configuration is that it
[starts about as fast as MRI](doc/contributor/svm.md), it may use less memory,
and it becomes fast in less time. The disadvantage of the native configuration
is that you can't use Java tools like VisualVM, you can't use Java
interoperability, and *peak performance may be lower than on the JVM*. The
native configuration is used by default, but you can also request it using
`--native`. To use polyglot programming with the *native* configuration, you
need to use the `--polyglot` flag.

TruffleRuby can also be used in the *JVM* configuration, where it runs as a
normal Java application on the JVM, as any other Java application would. The
advantage of the JVM configuration is that you can use Java interoperability,
and *peak performance may be higher than the native configuration*. The
disadvantage of the JVM configuration is that it takes much longer to start and
to get fast, and may use more memory. The JVM configuration is requested using
`--jvm`.

If you are running a short-running program you probably want the default,
*native*, configuration. If you are running a long-running program and want the
highest possible performance you probably want the *JVM* configuration, by using
`--jvm`.

At runtime you can tell if you are using the native configuration using
`Truffle.native?`.

You won't encounter it when using TruffleRuby from the GraalVM, but there is
also another configuration which is TruffleRuby running on the JVM but with the
Graal compiler not available. This configuration will have much lower
performance and should normally only be used for development.

## System Compatibility

TruffleRuby is actively tested on these systems:

* Oracle Linux 7
* Ubuntu 16.04 LTS
* Fedora 25
* macOS 10.13

You need to [install LLVM](doc/user/installing-llvm.md) to build and run C
extensions and [`zlib`](doc/user/installing-zlib.md) and
[`libssl`](doc/user/installing-libssl.md) for `openssl`. You may also need to
set up a [UTF-8 locale](doc/user/utf8-locale.md).

## Current Status

TruffleRuby is progressing fast but is currently probably not ready for you to
try running your full Ruby application on. However it is ready for
experimentation and curious end-users to try on their gems and smaller
applications.

TruffleRuby runs Rails, and passes the majority of the Rails test suite. But it
is missing support for Nokogiri and ActiveRecord database drivers which makes it
not practical to run real Rails applications at the moment.

You will find that many C extensions will not work without modification.

## Documentation

Extensive documentation is available in [`doc`](doc).
[`doc/user`](doc/user) documents how to use TruffleRuby and
[`doc/contributor`](doc/contributor) documents how to develop TruffleRuby.

## Contact

The best way to get in touch with us is to join us in
https://gitter.im/graalvm/truffleruby, but you can also Tweet to
[@TruffleRuby](https://twitter.com/truffleruby), or email
chris.seaton@oracle.com.

## Mailing list

Announcements about GraalVM, including TruffleRuby, are made on the
[graal-dev](http://mail.openjdk.java.net/mailman/listinfo/graal-dev) mailing list.

## Authors

The main authors of TruffleRuby in order of joining the project are:

* Chris Seaton
* Benoit Daloze
* Kevin Menard
* Petr Chalupa
* Brandon Fish
* Duncan MacGregor

Additionally:

* Thomas Würthinger
* Matthias Grimmer
* Josef Haider
* Fabio Niephaus
* Matthias Springer
* Lucas Allan Amorim
* Aditya Bhardwaj

Collaborations with:

* [Institut für Systemsoftware at Johannes Kepler University
   Linz](http://ssw.jku.at)

And others.

## Security

See the [security documentation](doc/user/security.md).

## Licence

TruffleRuby is copyright (c) 2013-2018 Oracle and/or its affiliates, and is made
available to you under the terms of any of three licenses:

* Eclipse Public License version 1.0, or
* GNU General Public License version 2, or
* GNU Lesser General Public License version 2.1.

TruffleRuby contains additional code not always covered by these licences, and
with copyright owned by other people. See
[doc/legal/legal.md](doc/legal/legal.md) for full documentation.

## Attribution

TruffleRuby is a fork of [JRuby](https://github.com/jruby/jruby), combining it
with code from the [Rubinius](https://github.com/rubinius/rubinius) project, and
also containing code from the standard implementation of Ruby,
[MRI](https://github.com/ruby/ruby).
