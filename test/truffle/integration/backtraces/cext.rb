# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative 'backtraces'

# See also test/truffle/cexts/backtraces for more tests

# Require some C ext to load C ext support
require 'syslog'

RB_FUNCALLV = Truffle::Interop.import('@rb_funcallv')

def callback
  raise 'Ruby callback error'
end

def ruby_call
  RB_FUNCALLV.call(self, 'callback', 0, nil)
end

def top
  ruby_call
end

check('cext_funcallv.backtrace') do
  top
end

# Example with an internal error
LOCK = Mutex.new
RB_MUTEX_LOCK = Truffle::Interop.import('@rb_mutex_lock')

LOCK.lock

def double_lock
  RB_MUTEX_LOCK.call(LOCK)
end

check('cext_double_lock.backtrace') do
  double_lock
end
