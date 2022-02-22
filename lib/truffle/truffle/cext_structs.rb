# truffleruby_primitives: true

# Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# This file includes the Ruby definition of C structs defined in ruby headers (or made available through it, for
# instance as function return value).

module Truffle::CExt
  def RDATA(object)
    # A specialized version of rb_check_type(object, T_DATA)
    data_holder = Primitive.object_hidden_var_get(object, DATA_HOLDER)
    unless data_holder
      raise TypeError, "wrong argument type #{object.class} (expected T_DATA)"
    end

    RData.new(object, data_holder)
  end

  def RDATA_PTR(object)
    # A specialized version of rb_check_type(object, T_DATA)
    data_holder = Primitive.object_hidden_var_get(object, DATA_HOLDER)
    unless data_holder
      raise TypeError, "wrong argument type #{object.class} (expected T_DATA)"
    end

    Primitive.data_holder_get_data(data_holder)
  end

  def RBASIC(object)
    if Primitive.immediate_value?(object)
      raise TypeError, "immediate values don't include the RBasic struct"
    end
    RBasic.new(object)
  end

  def RARRAY_PTR(array)
    RArrayPtr.new(array)
  end

  def RFILE(file)
    RFile.new(RBASIC(file), GetOpenFile(file))
  end
end

# ruby.h: `struct RData` and struct `RTypedData`
class Truffle::CExt::RData
  def initialize(object, data_holder)
    @object = object
    @data_holder = data_holder
  end

  private

  def polyglot_has_members?
    true
  end

  def polyglot_members(internal)
    %w[basic data type typed_flag]
  end

  def polyglot_read_member(name)
    case name
    when 'data'
      Primitive.cext_mark_object_on_call_exit(@object) unless Primitive.object_hidden_var_get(@object, Truffle::CExt::DATA_MARKER).nil?
      Primitive.data_holder_get_data(@data_holder)
    when 'type'
      type
    when 'typed_flag'
      type ? 1 : 0
    when 'basic'
      get_basic
    else
      raise Truffle::Interop::UnknownIdentifierException
    end
  end

  def polyglot_write_member(name, value)
    raise Truffle::Interop::UnknownIdentifierException unless name == 'data'
    Primitive.data_holder_set_data(@data_holder, value)
  end

  def polyglot_remove_member(name)
    raise Truffle::Interop::UnsupportedMessageException
  end

  def polyglot_invoke_member(name, *args)
    raise Truffle::Interop::UnsupportedMessageException
  end

  def polyglot_member_readable?(name)
    name == 'basic' or name == 'data' or name == 'type' or name == 'typed_flag'
  end

  def polyglot_member_modifiable?(name)
    name == 'data'
  end

  def polyglot_member_removable?(name)
    false
  end

  def polyglot_member_insertable?(name)
    false
  end

  def polyglot_member_invocable?(name)
    false
  end

  def polyglot_member_internal?(name)
    false
  end

  def polyglot_has_member_read_side_effects?(name)
    false
  end

  def polyglot_has_member_write_side_effects?(name)
    false
  end

  def get_basic
    @basic ||= Truffle::CExt::RBasic.new(@object)
  end

  def type
    Primitive.object_hidden_var_get(@object, Truffle::CExt::DATA_TYPE)
  end
end

# ruby.h: `struct RBasic`
class Truffle::CExt::RBasic
  USER_FLAGS = Primitive.object_hidden_var_create :user_flags

  # RUBY_FL* values are from ruby.h
  RUBY_FL_TAINT = (1<<8)
  RUBY_FL_FREEZE = (1<<11)

  RUBY_FL_USHIFT = 12
  USER_FLAGS_MASK = (1 << (RUBY_FL_USHIFT + 19)) - (1 << (RUBY_FL_USHIFT))
  private_constant :RUBY_FL_USHIFT, :USER_FLAGS_MASK

  def initialize(object)
    @object = object
  end

  private

  def user_flags
    Primitive.object_hidden_var_get(@object, USER_FLAGS) || 0
  end

  def set_user_flags(flags)
    Primitive.object_hidden_var_set(@object, USER_FLAGS, flags)
  end

  def compute_flags
    flags = 0
    flags |= RUBY_FL_FREEZE if @object.frozen?
    flags | user_flags
  end

  def flag_to_string(flag)
    case flag
    when 1<<5;        'RUBY_FL_WB_PROTECTED (1<<5)'
    when 1<<6;        'RUBY_FL_PROMOTED1 (1<<6)'
    when 1<<5 | 1<<6; 'RUBY_FL_PROMOTED (1<<5 | 1<<6)'
    when 1<<7;        'RUBY_FL_FINALIZE (1<<7)'
    when 1<<8;        'RUBY_FL_FREEZE (1<<8)'
    when 1<<10;       'RUBY_FL_EXIVAR (1<<10)'
    when 1<<11;       'RUBY_FL_TAINT (1<<11)'
    when 1<<2 | 1<<3; 'RUBY_FL_USHIFT (1<<2 | 1<<3)'
    else;             "unknown flag (#{flag})"
    end
  end

  def flags_to_string(flags)
    ushift   = flags[2] == 1 && flags[3] == 1
    promoted = flags[5] == 1 && flags[6] == 1

    decoded = (0...flags.bit_length).reject do |i|
      flags[i] == 0 ||
          ushift && (i == 2 || i == 3) ||
          promoted && (i == 5 || i == 6)
    end
    decoded = decoded.map { |i| 1 << i }

    decoded << (1<<5 | 1<<6) if promoted
    decoded << (1<<2 | 1<<3) if ushift
    decoded.map(&method(:flag_to_string)).join(', ')
  end

  def set_flags(flags)
    if flags & RUBY_FL_TAINT != 0
      flags &= ~RUBY_FL_TAINT
    end

    set_user_flags(flags & USER_FLAGS_MASK)
    flags &= ~USER_FLAGS_MASK

    # handle last!
    if flags & RUBY_FL_FREEZE != 0
      @object.freeze
      flags &= ~RUBY_FL_FREEZE
    elsif @object.frozen?
      raise ArgumentError, "can't unfreeze object"
    end


    raise ArgumentError, "unsupported remaining flags: #{flags_to_string(flags)}" if flags != 0
  end

  def polyglot_has_members?
    true
  end

  def polyglot_members(internal)
    %w[flags]
  end

  def polyglot_read_member(name)
    case name
    when 'flags'
      compute_flags
    when 'klass'
      Primitive.cext_wrap(Primitive.class_of(@object))
    else
      raise Truffle::Interop::UnknownIdentifierException
    end
  end

  def polyglot_write_member(name, value)
    raise Truffle::Interop::UnknownIdentifierException unless name == 'flags'
    set_flags value
  end

  def polyglot_remove_member(name)
    raise Truffle::Interop::UnsupportedMessageException
  end

  def polyglot_invoke_member(name, *args)
    raise Truffle::Interop::UnsupportedMessageException
  end

  def polyglot_member_readable?(name)
    name == 'flags' || name == 'klass'
  end

  def polyglot_member_modifiable?(name)
    name == 'flags'
  end

  def polyglot_member_removable?(name)
    false
  end

  def polyglot_member_insertable?(name)
    false
  end

  def polyglot_member_invocable?(name)
    false
  end

  def polyglot_member_internal?(name)
    false
  end

  def polyglot_has_member_read_side_effects?(name)
    false
  end

  def polyglot_has_member_write_side_effects?(name)
    true
  end
end

# ruby.h: `struct RArray`
class Truffle::CExt::RArrayPtr
  attr_reader :array

  def initialize(array)
    @array = array
  end

  def polyglot_pointer?
    Primitive.array_store_native?(@array)
  end

  def polyglot_as_pointer
    Primitive.cext_mark_object_on_call_exit(@array)
    Primitive.array_store_address(@array)
  end

  def polyglot_to_native
    Primitive.array_store_to_native(@array)
  end

  def polyglot_has_array_elements?
    true
  end

  def polyglot_array_size
    @array.size
  end

  def polyglot_read_array_element(index)
    Primitive.cext_wrap(@array[index])
  end

  def polyglot_write_array_element(index, value)
    @array[index] = Primitive.cext_unwrap(value)
  end

  def polyglot_array_element_readable?(index)
    index >= 0 && index < @array.size
  end

  def polyglot_array_element_modifiable?(index)
    index >= 0 && index < @array.size
  end

  def polyglot_array_element_insertable?(index)
    false
  end

  def polyglot_array_element_removable?(index)
    false
  end
end

# io.h: `struct rb_io_t`
class Truffle::CExt::RbIO
  def initialize(io)
    @io = io
    Primitive.object_hidden_var_set(io, Truffle::CExt::RB_IO_STRUCT, self)
    @tied_io_for_writing = false
  end

  private

  def polyglot_has_members?
    true
  end

  def polyglot_members(internal)
    ['stdio_file', 'fd', 'mode', 'pathv', 'pid', 'lineno', 'tied_io_for_writing']
  end

  def polyglot_read_member(name)
    case name
    when 'fd'
      Primitive.io_fd(@io)
    when 'mode'
      @io.instance_variable_get(:@mode)
    when 'pathv'
      Primitive.cext_wrap(@io.instance_variable_get(:@path))
    when 'tied_io_for_writing'
      Primitive.cext_wrap(@tied_io_for_writing)
    else
      raise Truffle::Interop::UnknownIdentifierException
    end
  end

  def polyglot_write_member(name, value)
    case name
    when 'mode'
      @io.instance_variable_set(:@mode, value)
    when 'pathv'
      @io.instance_variable_set(:@path, Primitive.cext_unwrap(value))
    when 'tied_io_for_writing'
      @tied_io_for_writing = Primitive.cext_unwrap(value)
    else
      raise Truffle::Interop::UnknownIdentifierException
    end
  end

  def polyglot_remove_member(name)
    raise Truffle::Interop::UnsupportedMessageException
  end

  def polyglot_invoke_member(name, *args)
    raise Truffle::Interop::UnsupportedMessageException
  end

  def polyglot_member_readable?(name)
    name == 'fd' || name == 'mode' || name == 'pathv' || 'tied_io_for_writing'
  end

  def polyglot_member_modifiable?(name)
    name == 'mode' || name == 'pathv' || 'tied_io_for_writing'
  end

  def polyglot_member_removable?(name)
    false
  end

  def polyglot_member_insertable?(name)
    false
  end

  def polyglot_member_invocable?(name)
    false
  end

  def polyglot_member_internal?(name)
    false
  end

  def polyglot_has_member_read_side_effects?(name)
    false
  end

  def polyglot_has_member_write_side_effects?(name)
    false
  end
end

# encoding.h: `struct rb_encoding`
class Truffle::CExt::RbEncoding
  ENCODING_CACHE = {} # Encoding => RbEncoding
  NATIVE_CACHE = {} # RbEncoding address => RbEncoding
  ENCODING_CACHE_MUTEX = Mutex.new

  private_class_method :new

  def self.get(encoding)
    ENCODING_CACHE_MUTEX.synchronize do
      ENCODING_CACHE.fetch(encoding) { |key| ENCODING_CACHE[key] = new(encoding) }
    end
  end

  def self.get_encoding_from_native(rbencoding_ptr)
    ENCODING_CACHE_MUTEX.synchronize do
      NATIVE_CACHE[rbencoding_ptr].encoding
    end
  end

  attr_reader :encoding

  def initialize(encoding)
    @encoding = encoding
    @pointer = nil
    @name = Truffle::CExt::LIBTRUFFLERUBY.RSTRING_PTR_IMPL(Primitive.cext_wrap(encoding.name))
  end

  private

  def polyglot_has_members?
    true
  end

  def polyglot_members(internal)
    ['name']
  end

  def polyglot_read_member(name)
    raise Truffle::Interop::UnknownIdentifierException unless name == 'name'
    @name
  end

  def polyglot_write_member(name, value)
    raise Truffle::Interop::UnsupportedMessageException
  end

  def polyglot_remove_member(name)
    raise Truffle::Interop::UnsupportedMessageException
  end

  def polyglot_invoke_member(name, *args)
    raise Truffle::Interop::UnsupportedMessageException
  end

  def polyglot_member_readable?(name)
    name == 'name'
  end

  def polyglot_member_modifiable?(name)
    false
  end

  def polyglot_member_removable?(name)
    false
  end

  def polyglot_member_insertable?(name)
    false
  end

  def polyglot_member_invocable?(name)
    false
  end

  def polyglot_member_internal?(name)
    false
  end

  def polyglot_has_member_read_side_effects?(name)
    false
  end

  def polyglot_has_member_write_side_effects?(name)
    false
  end

  def polyglot_pointer?
    !@pointer.nil?
  end

  def polyglot_to_native
    unless @pointer
      ENCODING_CACHE_MUTEX.synchronize do
        unless @pointer
          @pointer = Truffle::CExt::LIBTRUFFLERUBY.rb_encoding_to_native(@name)
          NATIVE_CACHE[Truffle::Interop.as_pointer(@pointer)] = self
        end
      end
    end
  end

  def polyglot_as_pointer
    pointer = @pointer
    raise Truffle::Interop::UnsupportedMessageException if Primitive.nil?(pointer)
    Truffle::Interop.as_pointer(pointer)
  end
end

class Truffle::CExt::RFile
  def initialize(basic, file)
    @basic = basic
    @fptr = file
  end

  def polyglot_has_members?
    true
  end

  def polyglot_members(internal)
    ['basic', 'fptr']
  end

  def polyglot_read_member(name)
    case name
    when 'basic'
      @basic
    when 'fptr'
      @fptr
    else
      raise Truffle::Interop::UnknownIdentifierException
    end
  end

  def polyglot_write_member(name, value)
    raise Truffle::Interop::UnsupportedMessageException
  end

  def polyglot_remove_member(name)
    raise Truffle::Interop::UnsupportedMessageException
  end

  def polyglot_invoke_member(name, *args)
    raise Truffle::Interop::UnsupportedMessageException
  end

  def polyglot_member_readable?(name)
    name == 'basic' || name == 'fptr'
  end

  def polyglot_member_modifiable?(name)
    false
  end

  def polyglot_member_removable?(name)
    false
  end

  def polyglot_member_insertable?(name)
    false
  end

  def polyglot_member_invocable?(name)
    false
  end

  def polyglot_member_internal?(name)
    false
  end

  def polyglot_has_member_read_side_effects?(name)
    false
  end

  def polyglot_has_member_write_side_effects?(name)
    true
  end
end
