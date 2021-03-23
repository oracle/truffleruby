# frozen_string_literal: true

# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle::Diggable

  def self.dig(obj, idxs)
    idxs_size = idxs.size
    n = 0
    while n < idxs_size
      idx = idxs[n]

      # We've inlined the logic for dig for three core classes - before we use that logic we need to check if the method has been monkey patched
      unless Primitive.vm_object_method_is_basic(obj, :dig)
        raise TypeError, "#{obj.class} does not have #dig method" unless obj.respond_to?(:dig)
        return obj.dig(*idxs[n...])
      end

      # This is the inlined logic for indexing
      case obj
      when Hash
        obj = obj[idx]
      when Array
        obj = obj.at(idx)
      when Struct
        begin
          obj = obj[idx]
        rescue IndexError, NameError
          obj = nil
        end
      end

      return nil if Primitive.nil?(obj)
      n += 1
    end

    obj
  end

end
