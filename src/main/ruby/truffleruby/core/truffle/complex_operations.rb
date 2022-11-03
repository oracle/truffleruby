# frozen_string_literal: true

# Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module ComplexOperations
    def self.convert_not_real_arguments(real, imag, exception)
      raise_exception = !exception.equal?(false)
      if nil.equal?(real) || nil.equal?(imag)
        return nil unless raise_exception
        raise TypeError, "can't convert nil into Complex"
      end
      imag = nil if Primitive.undefined?(imag)

      if Primitive.object_kind_of?(real, String)
        real = String::Complexifier.new(real).strict_convert(exception)
        return nil if real.nil?
      end

      if Primitive.object_kind_of?(imag, String)
        imag = String::Complexifier.new(imag).strict_convert(exception)
        return nil if imag.nil?
      end

      if Primitive.object_kind_of?(real, Complex) && !Primitive.object_kind_of?(real.imag, Float) && real.imag == 0
        real = real.real
      end

      if Primitive.object_kind_of?(imag, Complex) && !Primitive.object_kind_of?(imag.imag, Float) && imag.imag == 0
        imag = imag.real
      end

      if Primitive.object_kind_of?(real, Complex) && !Primitive.object_kind_of?(imag, Float) && imag == 0
        return real
      end

      if Primitive.nil? imag
        if Primitive.object_kind_of?(real, Numeric) && !real.real?
          return real
        elsif !Primitive.object_kind_of?(real, Numeric)
          if raise_exception
            return Truffle::Type.rb_convert_type(real, Complex, :to_c)
          else
            return Truffle::Type.rb_check_convert_type(real, Complex, :to_c)
          end
        else
          imag = 0
        end
      elsif Primitive.object_kind_of?(real, Numeric) && Primitive.object_kind_of?(imag, Numeric) && (!real.real? || !imag.real?)
        return real + imag * Complex.new(0, 1)
      end

      if !Primitive.nil?(imag) && !raise_exception && !Primitive.object_kind_of?(imag, Integer) &&
        !Primitive.object_kind_of?(imag, Float) && !Primitive.object_kind_of?(imag, Rational)
        return nil
      end

      Complex.rect(real, imag)
    end
  end
end
