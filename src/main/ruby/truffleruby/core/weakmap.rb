# frozen_string_literal: true

# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module ObjectSpace
  # WeakMap uses identity comparison semantics. The implementation assumes that the Java representation of objects
  # do compare (equals() and hashCode()) using object identity. This is the case for instances of RubyDynamicObject.
  #
  # However, results are unspecified if used with instances of TruffleObject that override equals() and
  # hashCode().
  #
  # Note that, following MRI, we disallow immediate (primitive) values and frozen objects as keys or value of
  # WeakMap. We could easily transcend this limitation as we do not modify objects like MRI does (it sets a finalizer).
  # However, keeping the limitation enables the above assumption of identity comparison.
  class WeakMap
    include Enumerable

    private def check_key_or_value(kv, type)
      klass = Truffle::Type.object_class(kv) # necessary to account for BasicObject et al.
      if klass.include?(ImmediateValue)
        raise ArgumentError, "WeakMap #{type} can't be an instance of #{klass}"
      elsif Truffle::KernelOperations.value_frozen?(kv)
        raise FrozenError, "WeakMap #{type} can't be a frozen object"
      end
    end

    def []=(key, value)
      check_key_or_value(key, 'key')
      check_key_or_value(value, 'value')
      Primitive.weakmap_aset(self, key, value)
    end

    def inspect
      str = "#<ObjectSpace::WeakMap:0x#{Primitive.kernel_to_hex(object_id)}"
      entries = self.entries
      str += ': ' if entries.length > 0
      entries.each do |k, v|
        str += ', ' unless str[-2] == ':'
        str += "#{Truffle::Type.rb_any_to_s(k)} => #{Truffle::Type.rb_any_to_s(v)}"
      end
      str + '>'
    end
  end
end
