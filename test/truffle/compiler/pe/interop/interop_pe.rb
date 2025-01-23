# Copyright (c) 2016, 2025 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module InteropPE
  class A
    attr_accessor :foo
    def initialize(value)
      @foo = value
    end
  end
end


# The Truffle API's these use are different from GraalVM 0.12 and Truffle 0.13
tagged example "Truffle::Interop.read_member(InteropPE::A.new(42), :@foo)", 42
tagged example "Truffle::Interop.read_member(InteropPE::A.new(42), '@foo')", 42
