# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative 'backtraces'

unless defined?(Truffle) && Truffle::Interop.mime_type_supported?('application/javascript')
  puts "JavaScript doesn't appear to be available - skipping polylgot backtrace tests"
  exit
end

def foo
  raise 'foo-message'
end

Polyglot.export_method :foo
Polyglot.eval 'js', "foo = Polyglot.import('foo')"

Polyglot.eval 'js', "function bar() { foo(); }"

Polyglot.eval 'js', "Polyglot.export('bar', bar)"
Polyglot.import_method :bar

def baz
  bar(self)
end

Polyglot.export_method :baz
Polyglot.eval 'js', "baz = Polyglot.import('baz')"

Polyglot.eval 'js', "function bob() { baz(); }"

Polyglot.eval 'js', "Polyglot.export('bob', bob)"
Polyglot.import_method :bob

check('javascript.backtrace') do
  bob(self)
end
