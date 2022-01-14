# frozen_string_literal: true

# Copyright (c) 2007-2015, Evan Phoenix and contributors
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of Rubinius nor the names of its contributors
#   may be used to endorse or promote products derived from this software
#   without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# Copyright (c) 2011, Evan Phoenix
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of the Evan Phoenix nor the names of its contributors
#   may be used to endorse or promote products derived from this software
#   without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# The Type module provides facilities for accessing various "type" related
# data about an object, as well as providing type coercion methods. These
# facilities are independent of the object and thus are more robust in the
# face of ad hoc monkeypatching.

##
# Namespace for coercion functions between various ruby objects.

module Truffle
  module Type
    def self.object_respond_to_no_built_in?(obj, name, include_private = false)
      meth = Primitive.vm_method_lookup obj, name
      !Primitive.nil?(meth) && !Primitive.vm_method_is_basic(meth)
    end

    def self.check_funcall_callable(obj, name)
      # TODO BJF Review rb_method_call_status
      Primitive.vm_method_lookup obj, name
    end

    # Returns an object of given class. If given object already is one, it is
    # returned. Otherwise tries obj.meth and returns the result if it is of the
    # right kind. TypeError is raised if the conversion method fails or the
    # conversion result is wrong.
    #
    # Prefer the more correct #rb_convert_type.
    def self.coerce_to(obj, cls, meth)
      if Primitive.object_kind_of?(obj, cls)
        obj
      else
        execute_coerce_to(obj, cls, meth)
      end
    end

    def self.execute_coerce_to(obj, cls, meth)
      begin
        ret = obj.__send__(meth)
      rescue
        coerce_to_failed obj, cls, meth
      end

      if Primitive.object_kind_of?(ret, cls)
        ret
      else
        coerce_to_type_error obj, ret, meth, cls
      end
    end

    def self.coerce_to_failed(object, klass, method)
      if Primitive.object_respond_to? object, :inspect, false
        raise TypeError, "Coercion error: #{object.inspect}.#{method} => #{klass} failed"
      else
        raise TypeError, "Coercion error: #{method} => #{klass} failed"
      end
    end

    def self.coerce_to_type_error(original, converted, method, klass)
      oc = object_class original
      cc = object_class converted
      msg = "failed to convert #{oc} to #{klass}: #{oc}\##{method} returned #{cc}"
      raise TypeError, msg
    end

    # MRI conversion macros and functions

    # See Primitive.rb_num2int, Primitive.rb_num2long, Primitive.rb_to_int which are not defined as methods for efficiency

    def self.rb_num2uint(val)
      num = Primitive.rb_num2long(val)
      check_uint(num)
      num
    end

    def self.rb_num2ulong(val)
      raise TypeError, 'no implicit conversion from nil to integer' if Primitive.nil? val

      if Primitive.object_kind_of?(val, Integer)
        if Primitive.integer_fits_into_long(val)
          val
        else
          rb_big2ulong(val)
        end
      elsif Primitive.object_kind_of?(val, Float)
        fval = val.to_int
        rb_num2ulong(fval)
      else
        rb_num2ulong(Primitive.rb_to_int(val))
      end
    end

    def self.rb_num2dbl(val)
      raise TypeError, 'no implicit conversion from nil to float' if Primitive.nil? val

      if Primitive.object_kind_of?(val, Float)
        val
      elsif Primitive.object_kind_of?(val, Integer)
        val.to_f
      elsif Primitive.object_kind_of?(val, Rational)
        val.to_f
      elsif Primitive.object_kind_of?(val, String)
        raise TypeError, 'no implicit conversion from to float from string'
      else
        rb_num2dbl(rb_to_f(val))
      end
    end

    def self.rb_big2dbl(val)
      val.to_f
    end

    def self.rb_big2ulong(val)
      check_ulong(val)
      Primitive.integer_ulong_from_bignum(val)
    end

    def self.rb_to_f(val)
      if Primitive.object_kind_of?(val, Float)
        val
      else
        res = convert_type(val, Float, :to_f, true)
        unless Primitive.object_kind_of?(res, Float)
          conversion_mismatch(val, Float, :to_f, res)
        end
        res
      end
    end

    # Fallback for Primitive.{rb_to_int, rb_num2int, rb_num2long}
    def self.rb_to_int_fallback(val)
      res = convert_type(val, Integer, :to_int, true)
      unless Primitive.object_kind_of?(res, Integer)
        conversion_mismatch(val, Integer, :to_int, res)
      end
      res
    end

    def self.conversion_mismatch(val, cls, meth, res)
      raise TypeError, "can't convert #{object_class(val)} to #{cls} (#{object_class(val)}##{meth} gives #{object_class(res)})"
    end

    def self.fits_into_int?(val)
      Integer === val && Primitive.integer_fits_into_int(val)
    end

    def self.fits_into_long?(val)
      Integer === val && Primitive.integer_fits_into_long(val)
    end

    def self.check_uint(val)
      unless Primitive.integer_fits_into_uint(val)
        raise RangeError, "integer #{val} too #{val < 0 ? 'small' : 'big'} to convert to `uint'"
      end
    end

    def self.check_ulong(val)
      unless Primitive.integer_fits_into_ulong(val)
        raise RangeError, "integer #{val} too #{val < 0 ? 'small' : 'big'} to convert to `ulong'"
      end
    end

    def self.rb_check_to_integer(val, meth)
      if Primitive.object_kind_of?(val, Integer)
        val
      else
        v = convert_type(val, Integer, meth, false)
        if Primitive.object_kind_of?(v, Integer)
          v
        else
          nil
        end
      end
    end

    def self.rb_check_to_float(val)
      return nil if !Primitive.object_kind_of?(val, Numeric)
      if Primitive.object_kind_of?(val, Float)
        val
      else
        v = convert_type(val, Float, :to_f, false)
        if Primitive.object_kind_of?(v, Float)
          v
        else
          nil
        end
      end
    end

    # Try to coerce obj to cls using meth.
    # Similar to coerce_to but returns nil if conversion fails.
    def self.rb_check_convert_type(obj, cls, meth)
      if Primitive.object_kind_of?(obj, cls)
        obj
      else
        v = convert_type(obj, cls, meth, false)
        if Primitive.nil? v
          nil
        elsif !Primitive.object_kind_of?(v, cls)
          raise TypeError, "can't convert #{object_class(obj)} to #{cls} (#{object_class(obj)}##{meth} gives #{object_class(v)})"
        else
          v
        end
      end
    end

    def self.rb_convert_type(obj, cls, meth)
      if Primitive.object_kind_of?(obj, cls)
        obj
      else
        v = convert_type(obj, cls, meth, true)
        unless Primitive.object_kind_of?(v, cls)
          raise TypeError, "can't convert #{object_class(obj)} to #{cls} (#{object_class(obj)}##{meth} gives #{object_class(v)})"
        end
        v
      end
    end

    # MRI: Check_Type / rb_check_type
    def self.rb_check_type(obj, cls)
      unless Primitive.object_kind_of?(obj, cls)
        raise TypeError, "wrong argument type #{object_class(obj)} (expected #{cls})"
      end
      obj
    end

    def self.convert_type(obj, cls, meth, raise_on_error)
      r = check_funcall(obj, meth)
      if Primitive.undefined?(r)
        if raise_on_error
          raise TypeError, Truffle::ExceptionOperations.conversion_error_message(meth, obj, cls)
        else
          nil
        end
      else
        r
      end
    end

    def self.check_funcall(recv, meth, args = [])
      if Truffle::Interop.foreign?(recv)
        if recv.respond_to?(meth)
          recv.__send__(meth, *args)
        else
          undefined
        end
      else
        respond = check_funcall_respond_to?(recv, meth, true)
        if respond == 0
          undefined
        elsif check_funcall_callable(recv, meth)
          recv.__send__(meth, *args)
        else
          check_funcall_missing(recv, meth, args, respond, true)
        end
      end
    end

    def self.check_funcall_respond_to?(obj, meth, priv)
      # TODO Review BJF vm_respond_to
      if object_respond_to_no_built_in?(obj, :respond_to?, true)
        if obj.__send__(:respond_to?, meth, true)
          1
        else
          0
        end
      else
        -1
      end
    end

    def self.check_funcall_missing(recv, meth, args, respond, priv = false)
      ret = basic_obj_respond_to_missing(recv, meth, priv)
      respond_to_missing = !Primitive.undefined?(ret)
      if respond_to_missing and !ret
        undefined
      elsif object_respond_to_no_built_in?(recv, :method_missing, true)
        begin
          recv.__send__(:method_missing, meth, *args)
        rescue NoMethodError
          # TODO BJF usually more is done here
          if Primitive.vm_method_lookup recv, meth
            ret = false
          else
            ret = respond_to_missing
          end
          if ret
            raise
          else
            undefined
          end
        end
      else
        undefined
      end
    end

    def self.basic_obj_respond_to_missing(obj, mid, priv)
      if object_respond_to_no_built_in?(obj, :respond_to_missing?, true)
        obj.__send__(:respond_to_missing?, mid, priv)
      else
        undefined
      end
    end

    ##
    # Uses the logic of [Array, Hash, String].try_convert.
    #
    def self.try_convert(obj, cls, meth)
      if Primitive.object_kind_of?(obj, cls)
        obj
      elsif !obj.respond_to?(meth)
        nil
      else
        execute_try_convert(obj, cls, meth)
      end
    end

    def self.execute_try_convert(obj, cls, meth)
      ret = obj.__send__(meth)

      if Primitive.nil?(ret) || Primitive.object_kind_of?(ret, cls)
        ret
      else
        raise TypeError, "Coercion error: obj.#{meth} did NOT return a #{cls} (was #{object_class(ret)})"
      end
    end

    # Specific coercion methods

    def self.coerce_to_comparison(a, b)
      if cmp = (a <=> b)
        cmp
      else
        raise ArgumentError, "comparison of #{a.inspect} with #{b.inspect} failed"
      end
    end

    INT_MIN = -2147483648
    INT_MAX = 2147483647

    def self.coerce_to_float(obj)
      case obj
      when Float
        obj
      when Numeric
        coerce_to obj, Float, :to_f
      when nil, true, false
        raise TypeError, "can't convert #{obj.inspect} into Float"
      else
        raise TypeError, "can't convert #{object_class(obj)} into Float"
      end
    end

    def self.coerce_to_regexp(pattern, quote=false)
      if Regexp === pattern
        pattern
      else
        pattern = StringValue(pattern)
        pattern = Regexp.quote(pattern) if quote
        Regexp.new(pattern)
      end
    end

    def self.coerce_to_encoding(obj)
      case obj
      when Encoding
        obj
      when String
        Encoding.find obj
      else
        Encoding.find StringValue(obj)
      end
    end

    def self.coerce_to_path(obj, check_null = true)
      if Primitive.object_kind_of?(obj, String)
        path = obj
      else
        if Primitive.object_respond_to? obj, :to_path, false
          obj = obj.to_path
        end

        path = StringValue(obj)
      end

      unless path.encoding.ascii_compatible?
        raise Encoding::CompatibilityError, "path name must be ASCII-compatible (#{path.encoding.name})"
      end
      check_null_safe(path) if check_null
      path
    end

    def self.coerce_to_symbol(obj)
      if Primitive.object_kind_of? obj, Symbol
        obj
      else
        obj = obj.to_str if obj.respond_to?(:to_str)
        coerce_to(obj, Symbol, :to_sym)
      end
    end

    # Equivalent of num_exact in MRI's time.c, used by Time methods.
    def self.coerce_to_exact_num(obj)
      if Primitive.object_kind_of? obj, Integer
        obj
      elsif Primitive.object_kind_of? obj, String
        raise TypeError, "can't convert #{obj} into an exact number"
      elsif Primitive.nil? obj
        raise TypeError, "can't convert nil into an exact number"
      else
        rb_check_convert_type(obj, Rational, :to_r) || coerce_to(obj, Integer, :to_int)
      end
    end

    def self.coerce_to_utc_offset(offset)
      offset = String.try_convert(offset) || offset

      if Primitive.object_kind_of? offset, String
        unless offset.encoding.ascii_compatible? && offset.match(/\A(\+|-)(\d\d):(\d\d)(?::(\d\d))?\z/)
          raise ArgumentError, '"+HH:MM" or "-HH:MM" expected for utc_offset'
        end

        offset = $2.to_i*60*60 + $3.to_i*60 + ( $4 || '0' ).to_i
        offset = -offset if $1.ord == 45
      else
        offset = Truffle::Type.coerce_to_exact_num(offset)
      end

      if Rational === offset
        offset = offset.round
      end

      if offset <= -86400 || offset >= 86400
        raise ArgumentError, 'utc_offset out of range'
      end

      offset
    end

    def self.coerce_to_bitwise_operand(obj)
      if Primitive.object_kind_of? obj, Float
        raise TypeError, "can't convert Float into Integer for bitwise arithmetic"
      end
      coerce_to obj, Integer, :to_int
    end

    # String helpers

    def self.check_null_safe(string)
      raise ArgumentError, 'string contains null byte' if string.include? "\0"
      string
    end

    def self.binary_string(string)
      string.force_encoding Encoding::BINARY
    end

    def self.external_string(string)
      string.force_encoding Encoding.default_external
    end

    def self.encode_string(string, enc)
      string.force_encoding enc
    end

    # Misc

    def self.rb_inspect(val)
      str = Truffle::Type.rb_obj_as_string(val.inspect)
      result_encoding = Encoding.default_internal || Encoding.default_external
      if str.ascii_only? || (result_encoding.ascii_compatible? && str.encoding == result_encoding)
        str
      else
        Primitive.string_escape str
      end
    end

    def self.rb_obj_as_string(obj)
      if Primitive.object_kind_of?(obj, String)
        obj
      else
        str = obj.to_s
        if Primitive.object_kind_of?(str, String)
          str
        else
          Truffle::Type.rb_any_to_s(obj)
        end
      end
    end

    def self.check_arity(arg_count, min, max)
      if arg_count < min || (max != -1 && arg_count > max)
        raise ArgumentError, arity_error_string(arg_count, min, max)
      end
    end

    def self.arity_error_string(arg_count, min, max)
      case
      when min == max
        'wrong number of arguments (given %d, expected %d)' % [arg_count, min]
      when max == -1
        'wrong number of arguments (given %d, expected %d+)' % [arg_count, min]
      else
        'wrong number of arguments (given %d, expected %d..%d)' % [arg_count, min, max]
      end
    end

    # Needs to be in core for assigning $!
    def self.set_last_exception(error)
      if Primitive.nil?(error) || Primitive.object_kind_of?(error, Exception)
        Primitive.thread_set_exception(error)
      else
        raise TypeError, 'assigning non-exception to ?!'
      end
    end

    def self.is_special_const?(object)
      # Avoid calling methods on object since it might be a foreign object
      NilClass === object || TrueClass === object || FalseClass === object || Symbol === object || Truffle::Type.fits_into_long?(object)
    end
  end
end
