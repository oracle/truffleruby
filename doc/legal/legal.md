# Legal Documentation

This document applies to TruffleRuby as built and distributed as the Native Standalone and JVM Standalone distributions,
which are the only supported ways to use TruffleRuby.

## TruffleRuby

TruffleRuby is copyright (c) 2013-2024 Oracle and/or its
affiliates, and is made available to you under the terms of any one of the
following three licenses:

* Eclipse Public License version 2.0, or
* GNU General Public License version 2, or
* GNU Lesser General Public License version 2.1.

See `epl-2.0.txt`, `gpl-2.txt`, `lgpl-2.1.txt`.

## MRI

The standard implementation of Ruby is MRI. TruffleRuby contains code from MRI
version 3.2.4, including:

* the standard library in `lib/mri`, 
* Ruby C extension API in `lib/cext/include` and `src/main/c/cext`, 
* C extensions in `src/main/c/{bigdecimal,date,etc,io-console,nkf,openssl,psych,rbconfig-sizeof,ripper,syslog,zlib}`

MRI is copyright Yukihiro Matsumoto. It is made available under the terms of the
2-clause BSD licence `ruby-bsdl.txt`, or a custom licence `ruby-licence.txt`.

### Fourth-party code

MRI itself includes some third-party code that we have then included. This
includes, but isn't limited to:

The general-purpose hash table library `src/main/c/cext/st.c` and
`lib/cext/include/ruby/st.h` was written originally be Peter Moore and is
public domain.

`lib/cext/include/ccan/{build_assert,check_type,container_of,str}` are all
utilities from CCAN and are public domain or available under the terms of the
CC0 public domain dedication, see `ccan-cc0.txt`.

`lib/cext/include/ccan/list` is a utility from CCAN and is available under the
terms of 'BSD-MIT', see `ccan-bsd-mit.txt`. Despite the filename 'BSD-MIT' this
is the conventional MIT licence.

RDoc Darkfish theme fonts under `lib/mri/rdoc/generator/template/darkfish/` are
available under the terms of the SIL Open Font License 1.1, see `ofl.txt`.

The header file `lib/cext/include/ruby/onigmo.h` is part of Onigmo, available
under the same 2-clause BSD licence as Ruby.

RubyGems, in `lib/mri/rubygems` is available under the same custom licence as
MRI, see `ruby-licence.txt`, or the MIT licence, see `mit.txt`.

The C implementation of YAML, `src/main/c/psych/yaml` is available under the
MIT licence, see `mit.txt`.

## JRuby

TruffleRuby contains code from JRuby 9.4.4.0, including Java implementation
code, build system, shell script launchers, standard library modified from MRI,
and so on.

Where original files had JRuby licence headers we have copied them over. In
general this code is available under any of these licences:

* Eclipse Public License version 2.0, or
* GNU General Public License version 2, or
* GNU Lesser General Public License version 2.1.

See `epl-2.0.txt`, `gpl-2.txt`, `lgpl-2.1.txt`.

Some libraries that were spun out of JRuby, such as ByteList, have been
incorporated into our source code. These were under the same copyright and
licence as JRuby in the first place, so we have considered them part of JRuby.

For historical information from JRuby, see `jruby-copying.txt`, but this will
now be out of date.

# Rubinius

TruffleRuby contains code from Rubinius 2.11, including core library
implementation in `src/main/ruby/truffleruby/core`. This is in some cases
copyright 2011 Evan Phoenix, and in other cases copyright 2007-2015 Evan Phoenix
and contributors, and released under the 3-clause BSD license. We have included
licence headers in these files which weren't in the original ones.

Some parts of the TruffleRuby Java code (such as the implementation of Rubinius
primitives) are derived from Rubinius C++ code which is copyright 2007-2015,
Evan Phoenix and contributors, and released under the 3-clause BSD license.

Some parts of the Ruby implementations of the standard library in `lib/truffle`
are copyright 2013 Brian Shirai and are licensed under the 3-clause BSD license.
In some cases this code is just code from MRI, and covered by their licence. In
some cases we have modified this code.

# Bundled gems

This list is from [bundled_gems](bundled_gems) and `grep licenses lib/gems/specifications/*.gemspec`.
Versions as used in MRI unless otherwise specified.

#### debug

debug is under the same copyright and licence as MRI (see `ruby-bsdl.txt`).

#### matrix

matrix is under the same copyright and licence as MRI (see `ruby-bsdl.txt`).

#### minitest

minitest is copyright Ryan Davis and is available under an MIT licence (see `mit.txt`).

#### net-ftp, net-imap, net-pop, net-smtp

These 4 bundled gems are under the same copyright and licence as MRI (see `ruby-bsdl.txt`).

#### power_assert

power_assert is copyright Kazuki Tsujimoto and is available under the same licence as MRI (see `ruby-bsdl.txt`).

#### prime

prime is under the same copyright and licence as MRI (see `ruby-bsdl.txt`).

#### rake

Rake is copyright Jim Weirich and is available under an MIT licence (see `mit.txt`).

#### rbs

rbs is copyright Soutaro Matsumoto and is available under the same licence as MRI (see `ruby-bsdl.txt`).

#### rexml

rexml is under the same copyright and licence as MRI (see `ruby-bsdl.txt`).

#### rss

rss is under the same copyright and licence as MRI (see `ruby-bsdl.txt`).

#### test-unit

test-unit is copyright Sutou Kouhei, Ryan Davis, and Nathaniel Talbott
and is available under the same licence as MRI (see `ruby-bsdl.txt`).

#### typeprof

typeprof is copyright Yusuke Endoh and is available under an MIT licence (see `mit.txt`).

# Other gems

#### json

The json gem is available under the same licence as MRI (see `ruby-bsdl.txt`).

#### RDoc

It's part of the standard library, not a bundled gem. RDoc is copyright
Dave Thomas and Eric Hodel and is available under the terms of the GPL 2 (see
`gpl-2.txt`), or the same custom licence as MRI (see `ruby-licence.txt`). Some
other files in RDoc have different, but compatible, licences detailed in the
files.

#### FFI

TruffleRuby includes parts of the FFI gem (version as described in [lib/truffle/ffi/version.rb](../../lib/truffle/ffi/version.rb)).
The FFI gem is copyright  2008-2016, Ruby FFI project contributors, and covered by the three-clause BSD licence (see `ffi.txt`).

#### Prism

TruffleRuby uses the [Prism](https://github.com/ruby/prism) Ruby parser
(version as described in [src/main/c/yarp/include/prism/version.h](../../src/main/c/yarp/include/prism/version.h)),
copyright Shopify Inc. and is available under an MIT licence (see `src/main/c/yarp/LICENSE.md`).

# Java dependencies

TruffleRuby has Java dependencies on these modules, which are then included in
the distribution:

#### JONI

TruffleRuby uses JONI (version as described in `mx.truffleruby/suite.py`). JONI
is copyright its authors and is released under an MIT licence (see `mit.txt`).

#### JCodings

TruffleRuby uses JCodings (version as described in `mx.truffleruby/suite.py`).
JCodings is copyright its authors and is released under an MIT licence (see
`mit.txt`).

## Patches

`lib/patches` contains patches to gems that are automatically applied when the
gems are loaded, and contain third party code from those gems, with permissive
licenses. We've added the licenses to the individual files.

`lib/patches/stdlib` patches code in the standard library.

## Ruby Specs

We do not distribute MSpec or the Ruby Specs, but they are both copyright 2008
Engine Yard and are released under an MIT licence (see `mit.txt`).

## FFI Specs

We do not distribute the FFI Specs, but they are copyright 2008-2014
Ruby-FFI contributors and are released under an MIT licence (see `mit.txt`).

## Written offer for source code

For any software that you receive from Oracle in binary form which is licensed
under an open source license that gives you the right to receive the source
code for that binary, you can obtain a copy of the applicable source code by
visiting http://www.oracle.com/goto/opensourcecode. If the source code for the
binary was not provided to you with the binary, you can also receive a copy of
the source code on physical media by submitting a written request to the
address listed below or by sending an email to Oracle using the following
link: http://www.oracle.com/goto/opensourcecode/request.

Oracle America, Inc.  
Attn: Senior Vice President  
Development and Engineering Legal  
500 Oracle Parkway, 10th Floor  
Redwood Shores, CA 94065  

Your request should include:

- The name of the binary for which you are requesting the source code
- The name and version number of the Oracle product containing the binary
- The date you received the Oracle product 
- Your name 
- Your company name (if applicable)
- Your return mailing address and email, and 
- A telephone number in the event we need to reach you. 

We may charge you a fee to cover the cost of physical media and processing.

Your request must be sent 

1. within three (3) years of the date you received the Oracle product that
included the binary that is the subject of your request, or

2. in the case of code licensed under the GPL v3 for as long as Oracle offers
spare parts or customer support for that product model.
