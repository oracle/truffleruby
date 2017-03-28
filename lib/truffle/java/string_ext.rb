# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

class String
  def to_java_bytes
    a = self.bytes
    ba = java.lang.reflect.Array.newInstance(java.lang.Byte.TYPE, a.size)
    a.each_with_index { |b, i| ba[i] = b }
    ba
  end

  def to_java_string
    JavaUtilities.wrap_java_value(::Truffle::Interop.to_java_string(self))
  end

  def self.from_java_bytes(ba)
    a = Array.new(ba.size)
    ba.each_with_index { |b, i| a[i] = b }
    a.pack('c*')
  end
end
