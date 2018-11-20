# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative 'backtraces'

js = 'application/javascript'

unless defined?(Truffle) && Truffle::Interop.mime_type_supported?(js)
  puts "JavaScript doesn't appear to be available - skipping polylgot backtrace tests"
  exit
end

def foo
  raise 'foo-message'
end

Truffle::Interop.export_method :foo
Truffle::Interop.eval js, "foo = Polyglot.import('foo')"

Truffle::Interop.eval js, "function bar() { foo(); }"

Truffle::Interop.eval js, "Polyglot.export('bar', bar)"
Truffle::Interop.import_method :bar

def baz
  bar(self)
end

Truffle::Interop.export_method :baz
Truffle::Interop.eval js, "baz = Polyglot.import('baz')"

Truffle::Interop.eval js, "function bob() { baz(); }"

Truffle::Interop.eval js, "Polyglot.export('bob', bob)"
Truffle::Interop.import_method :bob

check('javascript.backtrace') do
  bob(self)
end
