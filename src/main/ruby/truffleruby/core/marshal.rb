# frozen_string_literal: true

# Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

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

class BasicObject
  private def __marshal__(ms)
    out = ms.serialize_extended_object self
    out << 'o'
    cls = Primitive.object_class self
    name = Primitive.module_name cls
    out << ms.serialize(name.to_sym)
    out << ms.serialize_instance_variables_suffix(self, true)
  end
end

class Class
  private def __marshal__(ms)
    if singleton_class?
      raise TypeError, "singleton class can't be dumped"
    elsif Primitive.nil?(name) || name.empty?
      raise TypeError, "can't dump anonymous module #{self}"
    end

    "c#{ms.serialize_integer(name.length)}#{name}"
  end
end

class Module
  private def __marshal__(ms)
    raise TypeError, "can't dump anonymous module #{self}" if Primitive.nil?(name) || name.empty?
    "m#{ms.serialize_integer(name.length)}#{name}"
  end
end

class Float
  private def __marshal__(ms)
    if nan?
      str = 'nan'
    elsif zero?
      str = (1.0 / self) < 0 ? '-0' : '0'
    elsif infinite?
      str = self < 0 ? '-inf' : 'inf'
    else
      s, decimal, sign, digits = dtoa

      if decimal < -3 or decimal > digits
        str = s.insert(1, '.') << "e#{decimal - 1}"
      elsif decimal > 0
        str = s[0, decimal]
        digits -= decimal
        str << ".#{s[decimal, digits]}" if digits > 0
      else
        str = +'0.'
        str << '0' * -decimal if decimal != 0
        str << s[0, digits]
      end
    end

    sl = str.length
    if sign == 1
      ss = '-'
      sl += 1
    end

    Truffle::Type.binary_string("f#{ms.serialize_integer(sl)}#{ss}#{str}")
  end
end

class Exception
  # Custom marshal dumper for Exception. Rubinius exposes the exception message as an instance variable and their
  # dumper takes advantage of that. This dumper instead calls Exception#message to get the message, but is otherwise
  # identical.
  private def __marshal__(ms)
    out = ms.serialize_extended_object self
    out << 'o'
    cls = Primitive.object_class self
    name = Primitive.module_name cls
    out << ms.serialize(name.to_sym)

    ivars = Primitive.object_ivars(self)
    number_of_ivars = ivars.size + 2
    cause = self.cause
    out << ms.serialize_fixnum(cause ? number_of_ivars + 1 : number_of_ivars)
    out << ms.serialize(:mesg)
    out << ms.serialize(Primitive.exception_message(self))
    out << ms.serialize(:bt)
    out << ms.serialize(self.backtrace)
    if cause
      out << ms.serialize(:cause)
      out << ms.serialize(cause)
    end
    out << Truffle::Type.binary_string(ms.serialize_instance_variables(self, ivars))

    out
  end
end

class Time
  def __custom_marshal__(ms)
    out = ''.b

    # Order matters.
    extra_values = {}
    extra_values[:offset] = gmt_offset unless gmt?
    extra_values[:zone] = zone

    if nsec > 0
      # MRI serializes nanoseconds as a Rational using an
      # obscure and implementation-dependent method.
      # To keep compatibility we can just put nanoseconds
      # in the numerator and set the denominator to 1.
      extra_values[:nano_num] = nsec
      extra_values[:nano_den] = 1
    end

    ivars = Primitive.object_ivars(self)
    out << 'I'.b
    out << Truffle::Type.binary_string("u#{ms.serialize(self.class.name.to_sym)}")

    str = _dump
    out << ms.serialize_integer(str.length) + str

    count = ivars.size + extra_values.size
    out << ms.serialize_integer(count)

    ivars.each do |ivar|
      val = Primitive.object_ivar_get self, ivar
      out << ms.serialize(ivar)
      out << ms.serialize(val)
    end

    extra_values.each_pair do |key, value|
      out << ms.serialize(key)
      out << ms.serialize(value)
    end

    out
  end
end

module Marshal
  class State
    def serialize_encoding?(obj)
      enc = Primitive.encoding_get_object_encoding obj
      enc && enc != Encoding::BINARY
    end

    def serialize_encoding(obj)
      enc = Primitive.encoding_get_object_encoding obj
      case enc
      when Encoding::US_ASCII
        :E.__send__(:__marshal__, self) + false.__send__(:__marshal__, self)
      when Encoding::UTF_8
        :E.__send__(:__marshal__, self) + true.__send__(:__marshal__, self)
      else
        :encoding.__send__(:__marshal__, self) + serialize_string(enc.name)
      end
    end

    def set_object_encoding(obj, enc)
      case obj
      when String
        obj.force_encoding enc
      when Regexp
        obj.source.force_encoding enc
      when Symbol
        raise ArgumentError, 'The encoding of a Symbol should be processed before building the Symbol'
      end
    end

    def set_instance_variables(obj)
      construct_integer.times do
        ivar = get_symbol
        value = construct

        case ivar
        when :E
          if value
            set_object_encoding obj, Encoding::UTF_8
          else
            set_object_encoding obj, Encoding::US_ASCII
          end
          next
        when :encoding
          if enc = Encoding.find(value)
            set_object_encoding obj, enc
            next
          end
        end

        Primitive.object_ivar_set obj, prepare_ivar(ivar), value
      end
    end

    STRING_ALLOCATE = String.method(:__allocate__).unbind

    def construct_string
      bytes = get_byte_sequence.force_encoding(Encoding::ASCII_8BIT)

      if @user_class
        cls = get_user_class
        if cls < String
          obj = STRING_ALLOCATE.bind_call(cls)
        else
          allocate = cls.method(:__allocate__)
          if Primitive.same_methods?(allocate, STRING_ALLOCATE)
            # For example, String.clone falls in this case
            obj = allocate.call
          else
            raise ArgumentError, 'dump format error (user class)'
          end
        end

        Primitive.string_initialize(obj, bytes, Encoding::ASCII_8BIT)
      else
        obj = bytes
      end

      store_unique_object obj
    end
  end
end

class Range
  # Custom marshal dumper for Range. Rubinius exposes the three main values in Range (begin, end, excl) as
  # instance variables. MRI does not, but the values are encoded as instance variables within the marshal output from
  # MRI, so they both generate the same output, with the exception of the ordering of the variables. In TruffleRuby,
  # we do something more along the lines of MRI and as such, the default Rubinius handler for dumping Range doesn't
  # work for us because there are no instance variables to dump. This custom dumper explicitly encodes the three main
  # values so we generate the correct dump data.
  private def __marshal__(ms)
    out = ms.serialize_extended_object self
    out << 'o'
    cls = Primitive.object_class self
    name = Primitive.module_name cls
    out << ms.serialize(name.to_sym)

    ivars = self.instance_variables
    out << ms.serialize_integer(3 + ivars.size)
    out << ms.serialize(:begin)
    out << ms.serialize(self.begin)
    out << ms.serialize(:end)
    out << ms.serialize(self.end)
    out << ms.serialize(:excl)
    out << ms.serialize(self.exclude_end?)
    ivars.each do |ivar|
      val = Primitive.object_ivar_get self, ivar
      out << ms.serialize(ivar)
      out << ms.serialize(val)
    end
    out
  end
end

class NilClass
  private def __marshal__(ms)
    '0'.b
  end
end

class TrueClass
  private def __marshal__(ms)
    'T'.b
  end
end

class FalseClass
  private def __marshal__(ms)
    'F'.b
  end
end

class Symbol
  private def __marshal__(ms)
    if idx = ms.find_symlink(self)
      Truffle::Type.binary_string(";#{ms.serialize_integer(idx)}")
    else
      ms.add_symlink self
      ms.serialize_symbol(self)
    end
  end
end

class String
  private def __marshal__(ms)
    out =  ms.serialize_instance_variables_prefix(self)
    out << ms.serialize_extended_object(self)
    out << ms.serialize_user_class(self, String)
    out << ms.serialize_string(self)
    out << ms.serialize_instance_variables_suffix(self)
    out
  end
end

class Integer
  private def __marshal__(ms)
    ms.serialize_integer(self, 'i')
  end
end

class Regexp
  private def __marshal__(ms)
    str = self.source
    out =  ms.serialize_instance_variables_prefix(self)
    out << ms.serialize_extended_object(self)
    out << ms.serialize_user_class(self, Regexp)
    out << '/'
    out << ms.serialize_integer(str.length) + str
    out << (options & Regexp::OPTION_MASK).chr
    out << ms.serialize_instance_variables_suffix(self)

    out
  end
end

class Struct
  private def __marshal__(ms)
    out =  ms.serialize_instance_variables_prefix(self)
    out << ms.serialize_extended_object(self)

    out << 'S'

    out << ms.serialize(self.class.name.to_sym)
    out << ms.serialize_integer(self.length)

    self.each_pair do |name, value|
      out << ms.serialize(name)
      out << ms.serialize(value)
    end

    out << ms.serialize_instance_variables_suffix(self)

    out
  end
end

class Array
  private def __marshal__(ms)
    out =  ms.serialize_instance_variables_prefix(self)
    out << ms.serialize_extended_object(self)
    out << ms.serialize_user_class(self, Array)
    out << '['
    out << ms.serialize_integer(self.length)
    unless empty?
      each do |element|
        out << ms.serialize(element)
      end
    end
    out << ms.serialize_instance_variables_suffix(self)

    out
  end
end

class Hash
  private def __marshal__(ms)
    raise TypeError, "can't dump hash with default proc" if default_proc

    out =  ms.serialize_instance_variables_prefix(self)
    out << ms.serialize_extended_object(self)
    out << ms.serialize_user_class(self, Hash)
    out << (self.default ? '}' : '{')
    out << ms.serialize_integer(length)
    unless empty?
      each_pair do |key, val|
        out << ms.serialize(key)
        out << ms.serialize(val)
      end
    end
    out << (self.default ? ms.serialize(self.default) : '')
    out << ms.serialize_instance_variables_suffix(self)

    out
  end
end

class Time
  def self.__construct__(ms, data, ivar_index, has_ivar)
    obj = _load(data)
    ms.store_unique_object obj

    if ivar_index and has_ivar[ivar_index]
      ms.set_instance_variables obj
      has_ivar[ivar_index] = false
    end

    nano_num = obj.instance_variable_get(:@nano_num)
    nano_den = obj.instance_variable_get(:@nano_den)
    if nano_num && nano_den
      Primitive.time_set_nseconds(obj,
        Rational(nano_num, nano_den).to_i)
    end

    obj
  end
end

module Unmarshalable
  private def __marshal__(ms)
    raise TypeError, "marshaling is undefined for class #{self.class}"
  end
end

class Method
  include Unmarshalable
end

class Proc
  include Unmarshalable
end

class IO
  include Unmarshalable
end

class MatchData
  include Unmarshalable
end

module Marshal

  MAJOR_VERSION = 4
  MINOR_VERSION = 8

  VERSION_STRING = "\x04\x08".b

  # Here only for reference
  TYPE_NIL = ?0
  TYPE_TRUE = ?T
  TYPE_FALSE = ?F
  TYPE_FIXNUM = ?i

  TYPE_EXTENDED = ?e
  TYPE_UCLASS = ?C
  TYPE_OBJECT = ?o
  TYPE_DATA = ?d  # no specs
  TYPE_USERDEF = ?u
  TYPE_USRMARSHAL = ?U
  TYPE_FLOAT = ?f
  TYPE_BIGNUM = ?l
  TYPE_STRING = ?"
  TYPE_REGEXP = ?/
  TYPE_ARRAY = ?[
  TYPE_HASH = ?{
  TYPE_HASH_DEF = ?}
  TYPE_STRUCT = ?S
  TYPE_MODULE_OLD = ?M  # no specs
  TYPE_CLASS = ?c
  TYPE_MODULE = ?m

  TYPE_SYMBOL = ?:
  TYPE_SYMLINK = ?;

  TYPE_IVAR = ?I
  TYPE_LINK = ?@

  class State

    def initialize(stream, depth, proc)
      # shared
      @links = {}
      @symlinks = {}
      @symbols = []
      @objects = []

      # dumping
      @depth = depth

      # loading
      if stream
        @stream = stream
      else
        @stream = nil
      end

      if stream
        @consumed = 2
      else
        @consumed = 0
      end

      @modules = nil
      @has_ivar = []
      @proc = proc
      @call = true
      @user_class = nil
    end

    def const_lookup(name, type = nil)
      mod = Object

      parts = String(name).split '::'
      parts.each do |part|
        mod = if Truffle::Type.const_exists?(mod, part)
                Truffle::Type.const_get(mod, part, false)
              else
                begin
                  mod.const_missing(part)
                rescue NameError
                  raise ArgumentError, "undefined class/module #{name}"
                end
              end
      end

      if type and not mod.instance_of? type
        raise ArgumentError, "#{name} does not refer to a #{type}"
      end

      mod
    end

    def add_non_immediate_object(obj)
      return if Primitive.immediate_value?(obj)
      add_object(obj)
    end

    def add_object(obj)
      sz = @objects.size
      @objects[sz] = obj
      @links[obj.__id__] = sz
    end

    def add_symlink(obj)
      sz = @symbols.size
      @symbols[sz] = obj
      @symlinks[obj.__id__] = sz
    end

    def reserve_symlink
      sz = @symbols.size
      @symbols[sz] = nil
      sz
    end

    def assign_reserved_symlink(sz, obj)
      @symbols[sz] = obj
      @symlinks[obj.__id__] = sz
    end

    def construct(ivar_index = nil, call_proc = true)
      type = consume_byte()
      obj = case type
            when 48   # ?0
              nil
            when 84   # ?T
              true
            when 70   # ?F
              false
            when 99   # ?c
              construct_class
            when 109  # ?m
              construct_module
            when 77   # ?M
              construct_old_module
            when 105  # ?i
              construct_integer
            when 108  # ?l
              construct_bignum
            when 102  # ?f
              construct_float
            when 58   # ?:
              construct_symbol(ivar_index)
            when 34   # ?"
              construct_string
            when 47   # ?/
              construct_regexp
            when 91   # ?[
              construct_array
            when 123  # ?{
              construct_hash
            when 125  # ?}
              construct_hash_def
            when 83   # ?S
              construct_struct
            when 111  # ?o
              construct_object
            when 117  # ?u
              construct_user_defined(ivar_index)
            when 85   # ?U
              construct_user_marshal
            when 100  # ?d
              construct_data
            when 64   # ?@
              num = construct_integer

              begin
                obj = @objects.fetch(num)
                return obj
              rescue IndexError
                raise ArgumentError, 'dump format error (unlinked)'
              end

            when 59   # ?;
              num = construct_integer
              sym = @symbols[num]

              raise ArgumentError, 'bad symbol' unless sym

              return sym
            when 101  # ?e
              @modules ||= []

              name = get_symbol
              @modules << const_lookup(name, Module)

              obj = construct nil, false

              extend_object obj

              obj
            when 67   # ?C
              name = get_symbol
              @user_class = name

              construct nil, false

            when 73   # ?I
              ivar_index = @has_ivar.length
              @has_ivar.push true

              obj = construct ivar_index, false

              set_instance_variables obj if @has_ivar.pop

              obj
            else
              raise ArgumentError, "load error, unknown type #{type}"
            end

      @proc.call(obj) if call_proc and @proc and @call

      obj
    end

    def construct_class
      obj = const_lookup(get_byte_sequence.to_sym, Class)
      store_unique_object obj
      obj
    end

    def construct_module
      obj = const_lookup(get_byte_sequence.to_sym, Module)
      store_unique_object obj
      obj
    end

    def construct_old_module
      obj = const_lookup(get_byte_sequence.to_sym)
      store_unique_object obj
      obj
    end

    ARRAY_ALLOCATE = Array.method(:__allocate__).unbind
    ARRAY_APPEND = Array.instance_method(:<<)

    def construct_array
      if @user_class
        cls = get_user_class()
        if cls < Array
          obj = ARRAY_ALLOCATE.bind_call(cls)
        else
          # This is what MRI does, it's weird.
          obj = cls.allocate
          store_unique_object obj
          return obj
        end
      else
        obj = []
      end
      store_unique_object obj

      construct_integer.times do |_i|
        ARRAY_APPEND.bind_call(obj, construct)
      end

      obj
    end

    def construct_bignum
      sign = consume_byte() == 45 ? -1 : 1  # ?-
      size = construct_integer * 2

      result = 0

      data = consume size
      (0...size).each do |exp|
        result += (data.getbyte(exp) * 2**(exp*8))
      end

      obj = result * sign

      add_object obj
      obj
    end

    def construct_data
      name = get_symbol
      klass = const_lookup name, Class
      store_unique_object klass

      obj = klass.allocate

      # TODO ensure obj is a wrapped C pointer (T_DATA in MRI-land)

      store_unique_object obj

      unless Primitive.object_respond_to? obj, :_load_data, false
        raise TypeError,
              "class #{name} needs to have instance method `_load_data'"
      end

      obj._load_data construct

      obj
    end

    def construct_float
      s = get_byte_sequence

      if s == 'nan'
        obj = Float::NAN
      elsif s == 'inf'
        obj = 1.0 / 0.0
      elsif s == '-inf'
        obj = 1.0 / -0.0
      else
        obj = s.to_f
      end

      store_unique_object obj

      obj
    end

    def construct_hash
      obj = @user_class ? get_user_class.allocate : {}
      store_unique_object obj

      construct_integer.times do
        original_modules = @modules
        @modules = nil
        key = construct
        val = construct
        @modules = original_modules

        # Use Primitive.hash_store (an alias for []=) to get around subclass overrides
        Primitive.hash_store(obj, key, val)
      end

      obj
    end

    def construct_hash_def
      obj = @user_class ? get_user_class.allocate : {}
      store_unique_object obj

      construct_integer.times do
        key = construct
        val = construct
        obj[key] = val
      end

      obj.default = construct

      obj
    end

    def construct_integer
      c = consume_byte()

      # The format appears to be a simple integer compression format
      #
      # The 0-123 cases are easy, and use one byte
      # We've read c as unsigned char in a way, but we need to honor
      # the sign bit. We do that by simply comparing with the +128 values
      return 0 if c == 0
      return c - 5 if 4 < c and c < 128

      # negative, but checked known it's instead in 2's complement
      return c - 251 if 252 > c and c > 127

      # otherwise c (now in the 1 to 4 range) indicates how many
      # bytes to read to construct the value.
      #
      # Because we're operating on a small number of possible values,
      # it's cleaner to just unroll the calculate of each

      case c
      when 1
        consume_byte
      when 2
        consume_byte | (consume_byte << 8)
      when 3
        consume_byte | (consume_byte << 8) | (consume_byte << 16)
      when 4
        consume_byte | (consume_byte << 8) | (consume_byte << 16) |
                       (consume_byte << 24)

      when 255 # -1
        consume_byte - 256
      when 254 # -2
        (consume_byte | (consume_byte << 8)) - 65536
      when 253 # -3
        (consume_byte |
         (consume_byte << 8) |
         (consume_byte << 16)) - 16777216 # 2 ** 24
      when 252 # -4
        (consume_byte |
         (consume_byte << 8) |
         (consume_byte << 16) |
         (consume_byte << 24)) - 4294967296
      else
        raise "Invalid integer size: #{c}"
      end
    end

    def construct_regexp
      s = get_byte_sequence
      if @user_class
        obj = get_user_class.new s, consume_byte
      else
        obj = Regexp.new s, consume_byte
      end

      store_unique_object obj
    end

    def construct_struct
      name = get_symbol

      klass = const_lookup name, Class
      members = klass.members

      obj = klass.allocate
      store_unique_object obj

      construct_integer.times do |i|
        slot = get_symbol
        unless members[i].intern == slot
          raise TypeError, "struct #{klass} is not compatible (#{slot.inspect} for #{members[i].inspect})"
        end

        Primitive.object_hidden_var_set obj, slot, construct
      end

      obj
    end

    def construct_symbol(ivar_index)
      data = get_byte_sequence

      # We must use the next @symbols index for the Symbol being constructed.
      # However, constructing a Symbol might require to construct the :E or :encoding symbols,
      # and those must have a larger index (if they are serialized for the first time),
      # to be binary-compatible with CRuby and pass specs. So we reserve the index and assign it later.
      idx = reserve_symlink

      # A Symbol has no instance variables (it's frozen),
      # but we need to know the encoding before building the Symbol
      if ivar_index and @has_ivar[ivar_index]
        # This sets the encoding of the String
        set_instance_variables data
        @has_ivar[ivar_index] = false
      end

      obj = data.to_sym
      assign_reserved_symlink idx, obj

      obj
    end

    def construct_user_defined(ivar_index)
      name = get_symbol
      klass = const_lookup name, Class

      data = get_byte_sequence

      if Primitive.object_respond_to? klass, :__construct__, false
        return klass.__construct__(self, data, ivar_index, @has_ivar)
      end

      if ivar_index and @has_ivar[ivar_index]
        set_instance_variables data
        @has_ivar[ivar_index] = false
      end

      obj = klass.__send__ :_load, data

      add_object obj

      obj
    end

    def construct_user_marshal
      name = get_symbol

      klass = const_lookup name, Class
      obj = klass.allocate

      extend_object obj if @modules

      unless Primitive.object_respond_to? obj, :marshal_load, true
        raise TypeError, "instance of #{klass} needs to have method `marshal_load'"
      end

      store_unique_object obj
      obj.__send__ :marshal_load, construct
      obj
    end

    def extend_object(obj)
      until @modules.empty?
        mod = @modules.pop
        mod.__send__ :extend_object, obj
        mod.__send__ :extended, obj
      end
    end

    def find_link(obj)
      @links[obj.__id__]
    end

    def find_symlink(obj)
      @symlinks[obj.__id__]
    end

    def get_byte_sequence
      size = construct_integer
      consume size
    end

    def get_user_class
      cls = const_lookup @user_class, Class
      @user_class = nil
      cls
    end

    def get_symbol
      @call = false
      begin
        sym = construct(nil, false)
      ensure
        @call = true
      end

      unless Symbol === sym
        raise ArgumentError, "expected Symbol, got #{sym.inspect}"
      end

      sym
    end

    def prepare_ivar(ivar)
      ivar.to_s =~ /\A@/ ? ivar : :"@#{ivar}"
    end

    def serialize(obj)
      raise ArgumentError, 'exceed depth limit' if @depth == 0

      # How much depth we have left.
      @depth -= 1

      if link = find_link(obj)
        str = Truffle::Type.binary_string("@#{serialize_integer(link)}")
      else
        add_non_immediate_object obj

        # ORDER MATTERS.
        if Primitive.object_respond_to? obj, :marshal_dump, true
          str = serialize_user_marshal obj
        elsif Primitive.object_respond_to? obj, :_dump, true
          str = serialize_user_defined obj
        else
          str = obj.__send__ :__marshal__, self
        end
      end

      @depth += 1

      str
    end

    def serialize_extended_object(obj)
      str = +''
      Primitive.vm_extended_modules obj, -> mod do
        str << "e#{serialize(mod.name.to_sym)}"
      end
      Truffle::Type.binary_string(str)
    end

    def serialize_instance_variables_prefix(obj)
      ivars = Primitive.object_ivars(obj)
      !ivars.empty? || serialize_encoding?(obj) ? 'I'.b : ''.b
    end

    def serialize_instance_variables_suffix(obj, force = false)
      ivars = Primitive.object_ivars(obj)

      unless force or !ivars.empty? or serialize_encoding?(obj)
        return ''.b
      end

      count = ivars.size

      if serialize_encoding?(obj)
        str = serialize_integer(count + 1)
        str << serialize_encoding(obj)
      else
        str = serialize_integer(count)
      end

      str << serialize_instance_variables(obj, ivars)

      Truffle::Type.binary_string(str)
    end

    def serialize_instance_variables(obj, ivars)
      str = ''.b

      ivars.each do |ivar|
        val = Primitive.object_ivar_get obj, ivar
        str << serialize(ivar)
        str << serialize(val)
      end

      str
    end

    def serialize_integer(n, prefix = nil)
      if Truffle::Type.fits_into_int?(n)
        Truffle::Type.binary_string(prefix.to_s + serialize_fixnum(n))
      else
        serialize_bignum(n)
      end
    end

    def serialize_fixnum(n)
      if n == 0
        s = n.chr
      elsif n > 0 and n < 123
        s = (n + 5).chr
      elsif n < 0 and n > -124
        s = (256 + (n - 5)).chr
      else
        s = +"\0"
        cnt = 0
        4.times do
          s << (n & 0xff).chr
          n >>= 8
          cnt += 1
          break if n == 0 or n == -1
        end
        s[0] = (n < 0 ? 256 - cnt : cnt).chr
      end
      Truffle::Type.binary_string(s)
    end

    def serialize_bignum(n)
      str = (n < 0 ? +'l-' : +'l+')
      cnt = 0
      num = n.abs

      while num != 0
        str << (num & 0xff).chr
        num >>= 8
        cnt += 1
      end

      if cnt % 2 == 1
        str << "\0"
        cnt += 1
      end

      Truffle::Type.binary_string(str[0..1] + serialize_fixnum(cnt / 2) + str[2..-1])
    end

    def serialize_symbol(obj)
      str = obj.to_s
      mf = 'I' unless str.ascii_only? or str.encoding == Encoding::BINARY
      if mf
        me = serialize_integer(1) + serialize_encoding(obj.encoding)
      end
      mi = serialize_integer(str.bytesize)
      Truffle::Type.binary_string("#{mf}:#{mi}#{str.b}#{me}")
    end

    def serialize_string(str)
      output = Truffle::Type.binary_string("\"#{serialize_integer(str.bytesize)}")
      output + Truffle::Type.binary_string(str.dup)
    end

    def serialize_user_class(obj, cls)
      if obj.class != cls
        Truffle::Type.binary_string("C#{serialize(obj.class.name.to_sym)}")
      else
        ''.b
      end
    end

    def serialize_user_defined(obj)
      if Primitive.object_respond_to? obj, :__custom_marshal__, false
        return obj.__custom_marshal__(self)
      end

      str = obj.__send__ :_dump, @depth

      unless Primitive.object_kind_of? str, String
        raise TypeError, '_dump() must return string'
      end

      out = serialize_instance_variables_prefix(str)
      out << Truffle::Type.binary_string("u#{serialize(obj.class.name.to_sym)}")
      out << serialize_integer(str.length) + str
      out << serialize_instance_variables_suffix(str)

      out
    end

    def serialize_user_marshal(obj)
      val = obj.__send__ :marshal_dump

      add_non_immediate_object val

      cls = Primitive.object_class obj
      name = Primitive.module_name cls
      name = serialize(name.to_sym)
      marshaled = val.__send__ :__marshal__, self
      Truffle::Type.binary_string("U#{name}#{marshaled}")
    end

    def store_unique_object(obj)
      if Symbol === obj
        add_symlink obj
      else
        add_non_immediate_object obj
      end
      obj
    end

    def set_exception_variables(obj)
      construct_integer.times do
        ivar = get_symbol
        value = construct
        case ivar
        when :bt
          Primitive.exception_set_custom_backtrace obj, value
        when :mesg
          Primitive.exception_set_message obj, value
        when :cause
          Primitive.exception_set_cause obj, value
        else # Regular instance variable
          Primitive.object_ivar_set obj, ivar, value
        end
      end
    end

    def construct_object
      name = get_symbol
      klass = const_lookup name, Class

      if klass <= Range
        construct_range(klass)
      else
        obj = klass.allocate

        raise TypeError, 'dump format error' unless Object === obj

        store_unique_object obj
        if Primitive.object_kind_of? obj, Exception
          set_exception_variables obj
        else
          set_instance_variables obj
        end

        obj
      end
    end

    # Rubinius stores three main values in Range (begin, end, excl) as instance variables and as such, can use the
    # normal, generic object deserializer. In TruffleRuby, we do not expose these values as instance variables, in
    # keeping with MRI. Moreover, we have specialized versions of Ranges depending on these values, so changing them
    # after object construction would create optimization problems. Instead, we patch the Rubinius marshal loader here
    # to specifically handle Ranges by constructing a Range of the proper type using the deserialized main values and
    # then setting any custom instance variables afterward.
    def construct_range(klass)
      range_begin = nil
      range_end = nil
      range_exclude_end = false
      ivars = {}

      construct_integer.times do
        ivar = prepare_ivar(get_symbol)
        value = construct

        case ivar
        when :@begin then range_begin = value
        when :@end then range_end = value
        when :@excl then range_exclude_end = value
        else ivars[ivar] = value
        end
      end

      range = klass.new(range_begin, range_end, range_exclude_end)
      store_unique_object range

      ivars.each do |name, value|
        Primitive.object_ivar_set range, name, value
      end

      range
    end

  end

  class IOState < State
    def consume(bytes)
      @stream.read(bytes)
    end

    def consume_byte
      b = @stream.getbyte
      raise EOFError unless b
      b
    end
  end

  class StringState < State
    def initialize(stream, depth, prc)
      super stream, depth, prc

      if @stream
        @byte_array = stream.bytes
      end
    end

    def inspect
      "#<Marshal::StringState #{@stream[@consumed..-1].inspect}>"
    end

    def consume(bytes)
      raise ArgumentError, 'marshal data too short' if @consumed > @stream.bytesize
      data = @stream.byteslice @consumed, bytes
      @consumed += bytes
      data
    end

    def consume_byte
      raise ArgumentError, 'marshal data too short' if @consumed >= @stream.bytesize
      data = @byte_array[@consumed]
      @consumed += 1
      data
    end
  end

  def self.dump(obj, an_io=nil, limit=nil)
    unless limit
      if Primitive.object_kind_of? an_io, Integer
        limit = an_io
        an_io = nil
      else
        limit = -1
      end
    end

    depth = Primitive.rb_to_int limit
    ms = State.new nil, depth, nil

    if an_io
      unless Primitive.object_respond_to? an_io, :write, false
        raise TypeError, 'output must respond to write'
      end
      if Primitive.object_respond_to? an_io, :binmode, false
        an_io.binmode
      end
    end

    str = VERSION_STRING + ms.serialize(obj)

    if an_io
      an_io.write(str)
      an_io
    else
      str
    end
  end

  def self.load(obj, prc = nil)
    if Primitive.object_respond_to? obj, :to_str, false
      data = obj.to_s

      major = data.getbyte 0
      minor = data.getbyte 1

      ms = StringState.new data, nil, prc

    elsif Primitive.object_respond_to? obj, :read, false and
          Primitive.object_respond_to? obj, :getc, false
      ms = IOState.new obj, nil, prc

      major = ms.consume_byte
      minor = ms.consume_byte
    else
      raise TypeError, 'instance of IO needed'
    end

    if major != MAJOR_VERSION or minor > MINOR_VERSION
      raise TypeError, "incompatible marshal file format (can't be read)\n\tformat version #{MAJOR_VERSION}.#{MINOR_VERSION} required; #{major.inspect}.#{minor.inspect} given"
    end

    ms.construct
  end

  class << self
    alias_method :restore, :load
  end

end
