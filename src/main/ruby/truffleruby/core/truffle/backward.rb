# frozen_string_literal: true

# Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Used by old versions of concurrent-ruby

module Truffle

  class AtomicReference < TruffleRuby::AtomicReference
    alias_method :value, :get
    alias_method :value=, :set
  end

  module Primitive
    def self.logical_processors
      Truffle::System.available_processors
    end
  end

  module Truffle::System
    def self.full_memory_barrier
      TruffleRuby.full_memory_barrier
    end
  end

end

module TruffleRuby

  def self.sulong?
    warn 'TruffleRuby.sulong? has been replaced by TruffleRuby.cexts? and will be removed at a future release', uplevel: 1
    cexts?
  end

  def self.graal?
    warn 'TruffleRuby.graal? has been replaced by TruffleRuby.jit? and will be removed at a future release', uplevel: 1
    jit?
  end

end
