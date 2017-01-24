# Legal Documentation

## TruffleRuby

TruffleRuby is copyright (c) 2013-2017 Oracle and/or its
affiliates, and is made available to you under the terms of three licenses:

* Eclipse Public License version 1.0
* GNU General Public License version 2
* GNU Lesser General Public License version 2.1

See `epl-1.0.txt`, `gpl-2.txt`, `lgpl-2.1.txt`.

## MRI

The standard implementation of Ruby is MRI. TruffleRuby contains code from MRI,
including the standard library in `lib/ruby/truffle/mri` and the Ruby C
extension API in `lib/ruby/truffle/cext`.

MRI is copyright Yukihiro Matsumoto. It is made available under the terms of the
BSD licence `ruby-bsdl.txt`, or a custom licence `ruby-licence.txt`.

## JRuby

TruffleRuby contains code from JRuby 9.1.7.0, including Java implementation
code, build system, shell script launchers, standard library modified from MRI,
and so on.

Where original files had JRuby licence headers we have copied them over. In
general this code is available under these licences:

* Eclipse Public License version 1.0
* GNU General Public License version 2
* GNU Lesser General Public License version 2.1
* Common Public License 1.0 (only in some files)

See `epl-1.0.txt`, `gpl-2.txt`, `lgpl-2.1.txt`, `cpl-1.0.txt`.

Note that the JRuby project object model file declares the licenses as GPL 3 and
LGPL 3. This is an error, documented in
https://github.com/jruby/jruby/issues/4454 and fixed by the maintainers in
https://github.com/jruby/jruby/commit/cb165f2a903158a11e015ddaeb500fb95cb017b2
after the release of 9.1.7.0.

## In progress

I am still in the process of carefully documenting the TruffleRuby legal
situation after the fork from JRuby. See `jruby-copying.txt` for the rest of
the notes from that fork until I tidy them up into this document.
