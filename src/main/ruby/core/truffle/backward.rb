# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle::System
  
  # Used by old versions of concurrent-ruby

  def self.full_memory_barrier
    TruffleRuby.full_memory_barrier
  end

end

module Truffle
  
  # Used by old versions of concurrent-ruby

  class AtomicReference < TruffleRuby::AtomicReference
    alias_method :value, :get
    alias_method :value=, :set
  end

end
